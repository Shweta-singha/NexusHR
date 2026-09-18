"""
Spark SQL feature engineering over the raw, event-level tables from
generate_raw_events.py (employees, attendance_events, leave_requests,
overtime_log), plus the per-employee `attrited` label from
synthetic_attrition.csv.

Separate from build_features_spark.py on purpose: that script works from
the older, already-aggregated synthetic_attrition.csv and has its own
tested Pipeline (StringIndexer -> OneHotEncoder -> VectorAssembler); this
one demonstrates actual Spark SQL - spark.sql() over registered temp
views, joins, groupBy aggregations, and window functions (OVER,
PARTITION BY, ROWS BETWEEN) - which the pre-aggregated CSV never had raw
rows to support. Neither script depends on the other; both remain
independently runnable and testable.

Same synthetic-data caveat as generate_raw_events.py and generate_data.py:
every row here traces back to fabricated data, not real HR history.
"""

import os

from pyspark.sql import SparkSession

from generate_raw_events import DATA_DIR, load_base_employees

EMPLOYEES_CSV = os.path.join(DATA_DIR, "employees.csv")
ATTENDANCE_CSV = os.path.join(DATA_DIR, "attendance_events.csv")
LEAVE_CSV = os.path.join(DATA_DIR, "leave_requests.csv")
OVERTIME_CSV = os.path.join(DATA_DIR, "overtime_log.csv")

# Build-time derived file, not raw source data - regenerated on every run,
# lives under output/ which is already gitignored.
DERIVED_DIR = os.path.join(os.path.dirname(__file__), "output")
EMPLOYEE_ATTRITION_CSV = os.path.join(DERIVED_DIR, "employee_attrition_labels.csv")

RECENT_DAYS = 30
ROLLING_WINDOW_DAYS = 30
TREND_LOOKBACK_DAYS = 2 * RECENT_DAYS  # the trend's "prior" window is days 31-60 back


def build_spark_session() -> SparkSession:
    return (
        SparkSession.builder
        .appName("NexusHR-SparkSQLFeatureEngineering")
        .master("local[*]")
        .getOrCreate()
    )


def write_employee_attrition_labels() -> None:
    """employees.csv deliberately doesn't carry `attrited` (a training
    label, not a feature). department_attrition_rate below needs
    employee_id joined to attrited, so this writes a small derived
    employee_id/department/attrited CSV using generate_raw_events.py's own
    load_base_employees() - same row order, same employee_id assignment -
    rather than re-deriving employee_id differently here.
    """
    base_df = load_base_employees()
    os.makedirs(DERIVED_DIR, exist_ok=True)
    base_df[["employee_id", "department", "attrited"]].to_csv(EMPLOYEE_ATTRITION_CSV, index=False)


def build_attendance_trend_sql() -> str:
    return f"""
        WITH attendance_recent AS (
            SELECT employee_id, AVG(CASE WHEN status = 'Present' THEN 1.0D ELSE 0.0D END) AS recent_rate
            FROM attendance_events
            WHERE TO_DATE(date) > (SELECT DATE_SUB(MAX(TO_DATE(date)), {RECENT_DAYS}) FROM attendance_events)
            GROUP BY employee_id
        ),
        attendance_prior AS (
            SELECT employee_id, AVG(CASE WHEN status = 'Present' THEN 1.0D ELSE 0.0D END) AS prior_rate
            FROM attendance_events
            WHERE TO_DATE(date) <= (SELECT DATE_SUB(MAX(TO_DATE(date)), {RECENT_DAYS}) FROM attendance_events)
              AND TO_DATE(date) >  (SELECT DATE_SUB(MAX(TO_DATE(date)), {TREND_LOOKBACK_DAYS}) FROM attendance_events)
            GROUP BY employee_id
        )
        SELECT
            COALESCE(r.employee_id, p.employee_id) AS employee_id,
            ROUND(COALESCE(r.recent_rate, 0.0D) - COALESCE(p.prior_rate, 0.0D), 4) AS attendance_trend
        FROM attendance_recent r
        FULL OUTER JOIN attendance_prior p ON r.employee_id = p.employee_id
    """


def compute_attendance_trend(spark: SparkSession):
    return spark.sql(build_attendance_trend_sql())


def build_overtime_trend_sql() -> str:
    return f"""
        WITH overtime_recent AS (
            SELECT employee_id, SUM(overtime_hours) / {RECENT_DAYS}.0 AS recent_avg_daily
            FROM overtime_log
            WHERE TO_DATE(date) > (SELECT DATE_SUB(MAX(TO_DATE(date)), {RECENT_DAYS}) FROM overtime_log)
            GROUP BY employee_id
        ),
        overtime_prior AS (
            SELECT employee_id, SUM(overtime_hours) / {RECENT_DAYS}.0 AS prior_avg_daily
            FROM overtime_log
            WHERE TO_DATE(date) <= (SELECT DATE_SUB(MAX(TO_DATE(date)), {RECENT_DAYS}) FROM overtime_log)
              AND TO_DATE(date) >  (SELECT DATE_SUB(MAX(TO_DATE(date)), {TREND_LOOKBACK_DAYS}) FROM overtime_log)
            GROUP BY employee_id
        )
        SELECT
            COALESCE(r.employee_id, p.employee_id) AS employee_id,
            ROUND(COALESCE(r.recent_avg_daily, 0.0D) - COALESCE(p.prior_avg_daily, 0.0D), 4) AS overtime_trend
        FROM overtime_recent r
        FULL OUTER JOIN overtime_prior p ON r.employee_id = p.employee_id
    """


def compute_overtime_trend(spark: SparkSession):
    return spark.sql(build_overtime_trend_sql())


def run_pipeline(spark: SparkSession):
    employees = spark.read.csv(EMPLOYEES_CSV, header=True, inferSchema=True)
    attendance = spark.read.csv(ATTENDANCE_CSV, header=True, inferSchema=True)
    leave = spark.read.csv(LEAVE_CSV, header=True, inferSchema=True)
    overtime = spark.read.csv(OVERTIME_CSV, header=True, inferSchema=True)
    attrition_labels = spark.read.csv(EMPLOYEE_ATTRITION_CSV, header=True, inferSchema=True)

    employees.createOrReplaceTempView("employees")
    attendance.createOrReplaceTempView("attendance_events")
    leave.createOrReplaceTempView("leave_requests")
    overtime.createOrReplaceTempView("overtime_log")
    attrition_labels.createOrReplaceTempView("attrition_labels")

    # --- GROUP BY aggregations (one per raw table) ---

    attendance_agg_sql = f"""
        SELECT
            employee_id,
            ROUND(AVG(CASE WHEN status = 'Present' THEN 1.0D ELSE 0.0D END), 4) AS attendance_rate,
            SUM(CASE
                WHEN status = 'Absent'
                 AND TO_DATE(date) > (SELECT DATE_SUB(MAX(TO_DATE(date)), {RECENT_DAYS}) FROM attendance_events)
                THEN 1 ELSE 0
            END) AS recent_absence_count
        FROM attendance_events
        GROUP BY employee_id
    """
    spark.sql(attendance_agg_sql).createOrReplaceTempView("attendance_agg")

    leave_agg_sql = """
        SELECT employee_id, COUNT(*) AS leave_frequency
        FROM leave_requests
        GROUP BY employee_id
    """
    spark.sql(leave_agg_sql).createOrReplaceTempView("leave_agg")

    overtime_agg_sql = """
        SELECT employee_id, ROUND(SUM(overtime_hours), 2) AS total_overtime_hours
        FROM overtime_log
        GROUP BY employee_id
    """
    spark.sql(overtime_agg_sql).createOrReplaceTempView("overtime_agg")

    # --- 3 separate joins onto employees, not one mega-query ---
    # attendance_agg is INNER-joined: every employee has all 180 attendance
    # rows by construction, so it can never be missing. leave_agg and
    # overtime_agg are LEFT-joined with COALESCE: an employee can
    # legitimately have zero leave requests (and, in principle, zero
    # overtime entries), and that must show up as 0, not NULL or a dropped
    # employee row.
    joined_sql = """
        SELECT
            e.employee_id,
            e.department,
            a.attendance_rate,
            a.recent_absence_count,
            COALESCE(l.leave_frequency, 0) AS leave_frequency,
            COALESCE(o.total_overtime_hours, 0.0) AS total_overtime_hours
        FROM employees e
        JOIN attendance_agg a ON e.employee_id = a.employee_id
        LEFT JOIN leave_agg l ON e.employee_id = l.employee_id
        LEFT JOIN overtime_agg o ON e.employee_id = o.employee_id
    """
    spark.sql(joined_sql).createOrReplaceTempView("employee_features")

    # --- Window function 1: department_attrition_rate ---
    dept_attrition_sql = """
        SELECT
            employee_id,
            department,
            ROUND(AVG(attrited) OVER (PARTITION BY department), 4) AS department_attrition_rate
        FROM attrition_labels
    """
    print("\n--- Window function SQL: department_attrition_rate ---")
    print(dept_attrition_sql)
    spark.sql(dept_attrition_sql).createOrReplaceTempView("dept_attrition")

    # --- Window function 2: rolling_30_day_attendance ---
    # Trailing 30-row (working-day) attendance rate per employee, ordered
    # by date. ROWS BETWEEN, not a groupBy - this is the one that actually
    # needs a window function.
    rolling_sql = f"""
        SELECT
            employee_id,
            date,
            ROUND(
                AVG(CASE WHEN status = 'Present' THEN 1.0D ELSE 0.0D END) OVER (
                    PARTITION BY employee_id
                    ORDER BY date
                    ROWS BETWEEN {ROLLING_WINDOW_DAYS - 1} PRECEDING AND CURRENT ROW
                ),
                4
            ) AS rolling_30_day_attendance
        FROM attendance_events
    """
    print("\n--- Window function SQL: rolling_30_day_attendance ---")
    print(rolling_sql)
    spark.sql(rolling_sql).createOrReplaceTempView("rolling_attendance")

    # rolling_30_day_attendance is a per-employee-per-day value; folded down
    # to one row per employee (their rolling value as of the last day in
    # the window) so it can join 1:1 onto the rest of the feature table.
    latest_rolling_sql = """
        SELECT employee_id, rolling_30_day_attendance
        FROM (
            SELECT
                employee_id,
                rolling_30_day_attendance,
                ROW_NUMBER() OVER (PARTITION BY employee_id ORDER BY date DESC) AS rn
            FROM rolling_attendance
        ) ranked
        WHERE rn = 1
    """
    spark.sql(latest_rolling_sql).createOrReplaceTempView("latest_rolling")

    # --- Trend features: most-recent-30-days vs. prior-30-days ---
    # Built as two separately-filtered CTEs joined together, not LAG.
    # LAG compares a row to the row immediately before it in an ordered
    # sequence, which fits naturally when there's an open-ended run of
    # periods to walk across (e.g. month-over-month trend over a year).
    # Here there are exactly two fixed, named windows - "last 30 days" and
    # "the 30 before that" - so a CTE per window with a plain WHERE date
    # filter is more direct to read and audit than manufacturing an
    # artificial period column just to LAG across it, and it reuses the
    # same WHERE-based date-window pattern recent_absence_count already
    # uses above. FULL OUTER JOIN + COALESCE because an employee can have
    # zero matching rows in the (sparse) overtime log for one window and
    # not the other - that must show up as a trend against 0, not a
    # dropped employee.
    attendance_trend_sql = build_attendance_trend_sql()
    print("\n--- Trend SQL: attendance_trend ---")
    print(attendance_trend_sql)
    compute_attendance_trend(spark).createOrReplaceTempView("attendance_trend")

    overtime_trend_sql = build_overtime_trend_sql()
    print("\n--- Trend SQL: overtime_trend ---")
    print(overtime_trend_sql)
    compute_overtime_trend(spark).createOrReplaceTempView("overtime_trend")

    final_sql = """
        SELECT
            f.employee_id,
            f.department,
            f.attendance_rate,
            f.recent_absence_count,
            f.leave_frequency,
            f.total_overtime_hours,
            d.department_attrition_rate,
            r.rolling_30_day_attendance,
            COALESCE(at.attendance_trend, 0.0D) AS attendance_trend,
            COALESCE(ot.overtime_trend, 0.0D) AS overtime_trend
        FROM employee_features f
        JOIN dept_attrition d ON f.employee_id = d.employee_id
        JOIN latest_rolling r ON f.employee_id = r.employee_id
        LEFT JOIN attendance_trend at ON f.employee_id = at.employee_id
        LEFT JOIN overtime_trend ot ON f.employee_id = ot.employee_id
    """
    return spark.sql(final_sql)


def main():
    write_employee_attrition_labels()

    spark = build_spark_session()
    spark.sparkContext.setLogLevel("WARN")

    final_df = run_pipeline(spark)
    final_df.createOrReplaceTempView("final_features")

    employee_count = spark.sql("SELECT COUNT(*) AS c FROM employees").collect()[0]["c"]
    total = final_df.count()
    print(f"\nFinal feature table row count: {total} (employees: {employee_count})")
    assert total == employee_count, "Row count mismatch -- a join likely multiplied or dropped rows"

    print("\nSample of final feature table (5 employees):")
    final_df.orderBy("employee_id").show(5, truncate=False)

    print("\nattendance_trend / overtime_trend spread (min / max examples):")
    final_df.orderBy("attendance_trend").select(
        "employee_id", "attendance_trend", "overtime_trend"
    ).show(3, truncate=False)
    final_df.orderBy(final_df["attendance_trend"].desc()).select(
        "employee_id", "attendance_trend", "overtime_trend"
    ).show(3, truncate=False)

    spark.stop()


if __name__ == "__main__":
    main()
