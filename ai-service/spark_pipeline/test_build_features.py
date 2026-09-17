import csv

import pytest
from pyspark.sql import SparkSession

from build_features_spark import (
    CATEGORICAL_FEATURE,
    LABEL,
    NUMERIC_FEATURES,
    build_feature_pipeline,
    department_summary,
)

DEPARTMENTS = ["Engineering", "Sales", "HR", "Finance", "Marketing", "Operations"]

# columns: tenure_years, role_level, overtime_hours_monthly, absence_rate,
# leave_utilization_rate, months_since_promotion, salary_percentile_in_dept,
# job_satisfaction, department, attrited
SAMPLE_ROWS = [
    (5.2, 2, 3.0, 0.02, 0.40, 8.0, 55.0, 4, "Engineering", 0),
    (1.1, 1, 12.0, 0.09, 0.75, 2.0, 30.0, 2, "Sales", 1),
    (7.8, 3, 1.5, 0.01, 0.20, 18.0, 70.0, 5, "HR", 0),
    (0.5, 1, 15.0, 0.12, 0.85, 1.0, 25.0, 2, "Finance", 1),
    (3.3, 2, 6.0, 0.05, 0.55, 5.0, 48.0, 3, "Marketing", 0),
    (9.0, 4, 2.0, 0.01, 0.15, 24.0, 90.0, 5, "Operations", 0),
    (2.0, 2, 10.0, 0.08, 0.65, 3.0, 40.0, 3, "Sales", 1),
    (4.4, 2, 4.5, 0.03, 0.35, 10.0, 60.0, 4, "Finance", 0),
]


@pytest.fixture(scope="module")
def spark():
    session = (
        SparkSession.builder
        .appName("NexusHR-FeaturePipelineTests")
        .master("local[*]")
        .getOrCreate()
    )
    yield session
    session.stop()


@pytest.fixture(scope="module")
def sample_df(spark, tmp_path_factory):
    # Built via a small on-disk CSV (loaded through spark.read.csv, same as
    # build_features_spark.py's own data path) rather than
    # spark.createDataFrame(list_of_tuples, ...): the latter always routes
    # row deserialization through a Python worker, and that worker crashes
    # on this environment's Python 3.14 (a PySpark 4.0.1 x Python 3.14
    # socket-teardown incompatibility, unrelated to this pipeline's code).
    # spark.read.csv needs no Python worker at all, sidestepping it.
    columns = NUMERIC_FEATURES + [CATEGORICAL_FEATURE, LABEL]
    csv_path = tmp_path_factory.mktemp("data") / "sample.csv"
    with open(csv_path, "w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(columns)
        writer.writerows(SAMPLE_ROWS)

    return spark.read.csv(str(csv_path), header=True, inferSchema=True)


def test_build_feature_pipeline_produces_features_column(sample_df):
    pipeline = build_feature_pipeline()
    fitted = pipeline.fit(sample_df)
    result = fitted.transform(sample_df)

    assert "features" in result.columns
    assert result.count() == sample_df.count()
    assert result.filter(result["features"].isNull()).count() == 0


def test_department_summary_covers_all_departments(sample_df):
    summary = department_summary(sample_df)
    rows = summary.collect()

    assert len(rows) == 6
    assert {row[CATEGORICAL_FEATURE] for row in rows} == set(DEPARTMENTS)
    assert all(row["attrition_rate"] is not None for row in rows)
