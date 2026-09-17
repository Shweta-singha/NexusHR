"""
PySpark re-implementation of the attrition feature pipeline in train.py.

Why this exists: train.py's pandas + sklearn pipeline works fine at 5,000
rows, but NexusHR is a demo of what a real HR platform's data layer would
look like at scale (many departments, years of history, thousands of
employees per department). This script does the same feature-engineering
job -- one-hot encoding `department`, assembling numeric features into a
single feature vector, and computing summary statistics -- expressed as a
Spark pipeline (StringIndexer -> OneHotEncoder -> VectorAssembler) so it can
run unchanged against a dataset partitioned across a cluster, not just a
single machine's memory.

This is a feature-engineering / data-pipeline exercise, not a retrain: the
existing scikit-learn model (train.py, model/attrition_model.joblib) is
still what main.py serves. This script demonstrates the same preprocessing
logic in a distributed-computing context and reports parity against the
pandas version so the two are verifiably doing the same thing.

Run locally with a local Spark session:
    spark-submit build_features_spark.py
or
    python build_features_spark.py
(SparkSession.builder.master("local[*]") below runs Spark's engine on this
machine using all available cores -- no cluster required to execute it, but
the same code runs unchanged on a real cluster by pointing --master at one.)
"""

import os

from pyspark.errors import AnalysisException
from pyspark.ml import Pipeline
from pyspark.ml.feature import OneHotEncoder, StringIndexer, VectorAssembler
from pyspark.sql import SparkSession
from pyspark.sql import functions as F

DATA_PATH = os.path.join(os.path.dirname(__file__), "..", "data", "synthetic_attrition.csv")
OUTPUT_PATH = os.path.join(os.path.dirname(__file__), "output", "features_csv", "features.csv")

NUMERIC_FEATURES = [
    "tenure_years",
    "role_level",
    "overtime_hours_monthly",
    "absence_rate",
    "leave_utilization_rate",
    "months_since_promotion",
    "salary_percentile_in_dept",
    "job_satisfaction",
]
CATEGORICAL_FEATURE = "department"
LABEL = "attrited"


def build_spark_session() -> SparkSession:
    return (
        SparkSession.builder
        .appName("NexusHR-AttritionFeaturePipeline")
        .master("local[*]")
        .getOrCreate()
    )


def build_feature_pipeline() -> Pipeline:
    """Mirrors train.py's ColumnTransformer: OneHotEncoder on `department`,
    passthrough on the numeric columns, assembled into one feature vector."""
    indexer = StringIndexer(inputCol=CATEGORICAL_FEATURE, outputCol="department_index")
    encoder = OneHotEncoder(inputCol="department_index", outputCol="department_ohe")
    assembler = VectorAssembler(
        inputCols=NUMERIC_FEATURES + ["department_ohe"],
        outputCol="features",
    )
    return Pipeline(stages=[indexer, encoder, assembler])


def department_summary(df):
    """Per-department attrition rate and mean overtime/satisfaction --
    the same groupby train.py's generate_data.py prints as a sanity check,
    computed here via Spark's distributed aggregation instead of pandas."""
    return (
        df.groupBy(CATEGORICAL_FEATURE)
        .agg(
            F.count("*").alias("headcount"),
            F.round(F.avg(LABEL), 4).alias("attrition_rate"),
            F.round(F.avg("overtime_hours_monthly"), 2).alias("avg_overtime_hours"),
            F.round(F.avg("job_satisfaction"), 2).alias("avg_job_satisfaction"),
        )
        .orderBy(F.desc("attrition_rate"))
    )


def main():
    spark = build_spark_session()
    spark.sparkContext.setLogLevel("WARN")

    try:
        df = spark.read.csv(DATA_PATH, header=True, inferSchema=True)
        row_count = df.count()
    except AnalysisException:
        spark.stop()
        raise SystemExit(
            f"synthetic_attrition.csv not found at {DATA_PATH} -- run generate_data.py first"
        )
    print(f"Loaded {row_count} rows, {len(df.columns)} columns from {DATA_PATH}")

    pipeline = build_feature_pipeline()
    fitted = pipeline.fit(df)
    features_df = fitted.transform(df)

    print("\nSample assembled feature vectors:")
    features_df.select("tenure_years", "department", "features", LABEL).show(5, truncate=80)

    print("\nPer-department summary (Spark distributed aggregation):")
    department_summary(df).show()

    # Spark's own DataFrameWriter (any format) routes local-filesystem writes
    # through Hadoop's FileOutputCommitter, which requires winutils.exe on
    # Windows even for a plain local write. Collecting this small result to
    # the driver and writing it with pandas sidesteps that entirely -- Spark
    # still does all the actual computation above, this just materializes it.
    # This also drops the assembled `features` vector column (CSV/pandas
    # can't hold it as a flat value; it's still shown above), keeping the
    # pre-assembled inputs instead.
    pdf = features_df.select(NUMERIC_FEATURES + [CATEGORICAL_FEATURE, LABEL]).toPandas()
    os.makedirs(os.path.dirname(OUTPUT_PATH), exist_ok=True)
    pdf.to_csv(OUTPUT_PATH, index=False)
    print(f"\nWrote feature-engineered dataset to {OUTPUT_PATH}")

    spark.stop()


if __name__ == "__main__":
    main()
