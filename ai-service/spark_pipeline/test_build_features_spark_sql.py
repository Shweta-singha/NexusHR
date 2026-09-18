import pandas as pd
import pytest
from pyspark.sql import SparkSession

from build_features_spark_sql import (
    ATTENDANCE_CSV,
    EMPLOYEES_CSV,
    run_pipeline,
    write_employee_attrition_labels,
)

KNOWN_EMPLOYEE_ID = "EMP00001"


@pytest.fixture(scope="module")
def spark():
    session = (
        SparkSession.builder
        .appName("NexusHR-SparkSQLFeaturePipelineTests")
        .master("local[*]")
        .getOrCreate()
    )
    yield session
    session.stop()


@pytest.fixture(scope="module")
def final_df(spark):
    write_employee_attrition_labels()
    return run_pipeline(spark)


def test_row_count_matches_employee_count(spark, final_df):
    employee_count = spark.read.csv(EMPLOYEES_CSV, header=True, inferSchema=True).count()
    assert final_df.count() == employee_count


def test_known_employee_attendance_rate_matches_raw_csv(final_df):
    # Hand-verify directly from the raw CSV (not a hardcoded number), then
    # assert the Spark SQL pipeline's aggregate matches it.
    raw = pd.read_csv(ATTENDANCE_CSV)
    raw_for_employee = raw[raw["employee_id"] == KNOWN_EMPLOYEE_ID]
    expected_attendance_rate = round((raw_for_employee["status"] == "Present").mean(), 4)

    row = final_df.filter(final_df["employee_id"] == KNOWN_EMPLOYEE_ID).collect()[0]

    assert row["attendance_rate"] == pytest.approx(expected_attendance_rate, abs=1e-4)
