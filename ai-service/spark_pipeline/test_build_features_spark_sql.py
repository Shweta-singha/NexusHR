import csv
import datetime

import pandas as pd
import pytest
from pyspark.sql import SparkSession

from build_features_spark_sql import (
    ATTENDANCE_CSV,
    EMPLOYEES_CSV,
    compute_attendance_trend,
    compute_overtime_trend,
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


def _write_csv(path, header, rows):
    with open(path, "w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(header)
        writer.writerows(rows)


def test_attendance_trend_detects_recent_decline(spark, tmp_path):
    # Constructed, not the real 900K-row dataset: 60 consecutive days for
    # one employee, all Present in the first 30 (the "prior" window) and
    # all Absent in the last 30 (the "recent" window) - a maximally clear
    # decline, so attendance_trend must come out negative.
    dates = [datetime.date(2025, 1, 1) + datetime.timedelta(days=i) for i in range(60)]
    rows = [
        ("EMP_TEST_DECLINE", d.strftime("%Y-%m-%d"), "Present" if i < 30 else "Absent")
        for i, d in enumerate(dates)
    ]
    csv_path = tmp_path / "attendance_trend_test.csv"
    _write_csv(csv_path, ["employee_id", "date", "status"], rows)

    # run_pipeline() always re-reads and re-registers the real CSVs itself
    # at the top of every call, so overriding this view here can't leak
    # into or corrupt the `final_df` fixture used by the other tests.
    spark.read.csv(str(csv_path), header=True, inferSchema=True).createOrReplaceTempView(
        "attendance_events"
    )
    trend_df = compute_attendance_trend(spark)
    row = trend_df.filter(trend_df["employee_id"] == "EMP_TEST_DECLINE").collect()[0]

    assert row["attendance_trend"] < 0
    assert row["attendance_trend"] == pytest.approx(-1.0, abs=1e-4)  # 0.0 recent - 1.0 prior


def test_overtime_trend_detects_recent_increase(spark, tmp_path):
    # Same structure, using overtime_log: 1.0 hour/day for the first 30
    # days (prior), 5.0 hours/day for the last 30 (recent) - a clear
    # increase, so overtime_trend must come out positive.
    dates = [datetime.date(2025, 1, 1) + datetime.timedelta(days=i) for i in range(60)]
    rows = [
        ("EMP_TEST_OVERTIME_UP", d.strftime("%Y-%m-%d"), 1.0 if i < 30 else 5.0)
        for i, d in enumerate(dates)
    ]
    csv_path = tmp_path / "overtime_trend_test.csv"
    _write_csv(csv_path, ["employee_id", "date", "overtime_hours"], rows)

    spark.read.csv(str(csv_path), header=True, inferSchema=True).createOrReplaceTempView(
        "overtime_log"
    )
    trend_df = compute_overtime_trend(spark)
    row = trend_df.filter(trend_df["employee_id"] == "EMP_TEST_OVERTIME_UP").collect()[0]

    assert row["overtime_trend"] > 0
    assert row["overtime_trend"] == pytest.approx(4.0, abs=1e-4)  # 5.0 recent - 1.0 prior avg/day
