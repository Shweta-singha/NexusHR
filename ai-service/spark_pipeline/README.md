# Spark feature pipeline

`build_features_spark.py` re-implements the feature-engineering half of
`train.py` (one-hot encoding `department`, assembling the numeric columns
into a single feature vector, and computing per-department summary stats)
as a PySpark ML `Pipeline` instead of pandas + scikit-learn. It's a
feature-engineering / data-pipeline demo, not a retrain: the scikit-learn
model in `model/attrition_model.joblib` is still what `main.py` serves, and
nothing in the deployed `ai-service` imports or runs this code. It only
runs locally via `python build_features_spark.py` or the tests below.

Run it with `pip install -r requirements-spark.txt` (kept separate from
`requirements.txt` since `main.py` never needs it, and the Dockerfile never
installs it).

## Output write: pandas, not Spark's own writer

The script writes its result with `.toPandas().to_csv(...)` instead of
`DataFrameWriter.csv()`/`.parquet()`. On Windows, any Spark file write goes
through Hadoop's `FileOutputCommitter`, which requires `winutils.exe` /
`HADOOP_HOME` even for a purely local write with no cluster involved -
without it, `.write.csv()` and `.write.parquet()` both fail identically
with `HADOOP_HOME and hadoop.home.dir are unset`, confirmed by testing
both. Collecting the small result to the driver and writing it with pandas
sidesteps Hadoop's commit protocol entirely; Spark still does the actual
computation (the ML pipeline fit/transform and the `groupBy` aggregation),
this only changes how the result is materialized to disk. The written CSV
also omits the assembled `features` vector column, since CSV can't hold a
vector type - it keeps the pre-assembled numeric/categorical columns that
went into it instead.

## Tests

`test_build_features.py` covers `build_feature_pipeline()` (asserts a
`features` column comes out, with no nulls, and the row count is
unchanged) and `department_summary()` (asserts exactly 6 rows, one per
department, each with a non-null `attrition_rate`). The test fixture
builds its sample rows through a small on-disk CSV loaded via
`spark.read.csv`, not `spark.createDataFrame(rows, ...)` - the latter
crashes on this environment's Python 3.14 (a PySpark 4.0.1 / Python 3.14
incompatibility in the Python worker's socket teardown, unrelated to this
pipeline's code). Run with `pytest test_build_features.py -v` from this
directory.
