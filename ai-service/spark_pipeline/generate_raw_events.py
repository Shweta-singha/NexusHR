"""
Generates SYNTHETIC event-level HR data - daily attendance, leave requests,
and an overtime log - FROM the per-employee summary statistics already in
data/synthetic_attrition.csv, plus an employees.csv extracted from the same
source.

Why this exists: generate_data.py's synthetic_attrition.csv has one
pre-aggregated summary number per employee (absence_rate,
leave_utilization_rate, overtime_hours_monthly). Real Spark SQL feature
engineering - joins, groupBy aggregations, window functions - needs raw
event-level rows to aggregate FROM, not a table that's already aggregated.
This script fabricates plausible day-by-day attendance, individual leave
requests, and individual overtime log entries that, in aggregate, land
close to each employee's existing summary numbers.

Same caveat as generate_data.py: every row in every file this script
writes is fabricated by the models below, not derived from any real
employee's actual attendance, leave, or overtime history. Treat it purely
as a stand-in for what a real event-level HR data layer would look like,
never as real historical fact, in any downstream code or report.

employee_id, department, and every other employee-level field here are
read directly from the already-committed data/synthetic_attrition.csv
(not regenerated from generate_data.py's rng), so employees.csv and the
three event tables line up row-for-row with it by construction - the same
file in, the same employee_id assignment every run.
"""

import datetime
import os

import numpy as np
import pandas as pd

SEED = 42  # same seed generate_data.py uses, for this script's own reproducibility

DATA_DIR = os.path.join(os.path.dirname(__file__), "..", "data")
ATTRITION_CSV = os.path.join(DATA_DIR, "synthetic_attrition.csv")

N_DAYS = 180  # working days covered by attendance_events.csv and overtime_log.csv
START_DATE = pd.Timestamp("2025-01-01")

# Attendance: Absent probability is each employee's own absence_rate; Late is
# a smaller, independent probability scaled off their non-absent days so
# Absent + Late can never exceed 1.
LATE_PROB_FACTOR = 0.05

# Leave: assumes a 20-day annual entitlement, purely to turn a 0-1
# utilization rate into a plausible total days taken - not a real company
# leave policy. Requests are 1-5 days each, chunked from that total.
ANNUAL_LEAVE_ENTITLEMENT_DAYS = 20
LEAVE_TYPES = ["Annual", "Sick", "Personal", "Unpaid"]
LEAVE_TYPE_PROBS = [0.55, 0.25, 0.15, 0.05]

# Overtime: assumes a flat 30% chance of an overtime entry on any given
# working day, then calibrates the average hours-per-entry per employee so
# that (entry probability x hours per entry x working days/month) reproduces
# their existing overtime_hours_monthly average.
WORKDAYS_PER_MONTH = 21
OVERTIME_DAY_PROB = 0.3


def load_base_employees() -> pd.DataFrame:
    df = pd.read_csv(ATTRITION_CSV)
    df.insert(0, "employee_id", [f"EMP{i:05d}" for i in range(1, len(df) + 1)])
    return df


def build_employees(base_df: pd.DataFrame) -> pd.DataFrame:
    return base_df[[
        "employee_id",
        "department",
        "tenure_years",
        "role_level",
        "salary_percentile_in_dept",
        "job_satisfaction",
        "months_since_promotion",
    ]].copy()


def build_attendance_events(base_df: pd.DataFrame, rng: np.random.Generator) -> pd.DataFrame:
    n = len(base_df)
    dates = pd.bdate_range(start=START_DATE, periods=N_DAYS).strftime("%Y-%m-%d").to_numpy()

    absence_rate = base_df["absence_rate"].to_numpy()[:, None]
    late_prob = LATE_PROB_FACTOR * (1 - absence_rate)

    draw = rng.uniform(size=(n, N_DAYS))
    status = np.full((n, N_DAYS), "Present", dtype=object)
    status[draw < absence_rate] = "Absent"
    status[(draw >= absence_rate) & (draw < absence_rate + late_prob)] = "Late"

    return pd.DataFrame({
        "employee_id": np.repeat(base_df["employee_id"].to_numpy(), N_DAYS),
        "date": np.tile(dates, n),
        "status": status.reshape(-1),
    })


def build_leave_requests(base_df: pd.DataFrame, rng: np.random.Generator) -> pd.DataFrame:
    n = len(base_df)
    used_days_target = base_df["leave_utilization_rate"].to_numpy() * ANNUAL_LEAVE_ENTITLEMENT_DAYS

    # Total days taken is drawn once, around the target, with modest noise.
    # Splitting that total into randomly-sized requests below adds no extra
    # noise to the total itself - it only decides how it's chunked up - so
    # the aggregate stays consistent with leave_utilization_rate. (Drawing
    # request *count* and request *length* as two independent noisy steps
    # was tried first and compounded into far more noise than the target
    # itself has, which is why this draws the total directly instead.)
    total_days = np.round(np.clip(
        rng.normal(loc=used_days_target, scale=used_days_target * 0.15 + 0.5),
        0, None,
    )).astype(int)

    # Calendar span wide enough to spread requests across roughly the same
    # period attendance_events/overtime_log cover (180 working days).
    span_days = int(N_DAYS / 5 * 7)
    employee_ids = base_df["employee_id"].to_numpy()

    records = []
    for idx in range(n):
        remaining = int(total_days[idx])
        while remaining > 0:
            length = min(int(rng.integers(1, 6)), remaining)
            start_offset = int(rng.integers(0, max(span_days - 5, 1)))
            leave_type = rng.choice(LEAVE_TYPES, p=LEAVE_TYPE_PROBS)
            start = START_DATE + datetime.timedelta(days=start_offset)
            end = start + datetime.timedelta(days=length - 1)
            records.append((
                employee_ids[idx],
                start.strftime("%Y-%m-%d"),
                end.strftime("%Y-%m-%d"),
                leave_type,
            ))
            remaining -= length

    return pd.DataFrame(
        records,
        columns=["employee_id", "leave_start_date", "leave_end_date", "leave_type"],
    )


def build_overtime_log(base_df: pd.DataFrame, rng: np.random.Generator) -> pd.DataFrame:
    n = len(base_df)
    dates = pd.bdate_range(start=START_DATE, periods=N_DAYS).strftime("%Y-%m-%d").to_numpy()

    mean_hours_per_entry = (
        base_df["overtime_hours_monthly"].to_numpy() / (WORKDAYS_PER_MONTH * OVERTIME_DAY_PROB)
    )

    occurs = rng.uniform(size=(n, N_DAYS)) < OVERTIME_DAY_PROB
    hours = rng.exponential(scale=mean_hours_per_entry[:, None], size=(n, N_DAYS))
    hours = np.clip(hours, 0.5, 8.0)

    emp_idx, day_idx = np.nonzero(occurs)
    employee_ids = base_df["employee_id"].to_numpy()

    return pd.DataFrame({
        "employee_id": employee_ids[emp_idx],
        "date": dates[day_idx],
        "overtime_hours": np.round(hours[emp_idx, day_idx], 2),
    }).sort_values(["employee_id", "date"]).reset_index(drop=True)


def print_consistency_check(base_df, attendance_df, leave_df, overtime_df):
    print("\n--- Consistency check: attendance_events vs absence_rate ---")
    actual_absence = (
        attendance_df.assign(is_absent=attendance_df["status"] == "Absent")
        .groupby("employee_id")["is_absent"].mean()
    )
    check = base_df.set_index("employee_id")[["absence_rate"]].join(
        actual_absence.rename("actual_absent_rate")
    )
    print(check.sample(5, random_state=SEED))
    print(f"Correlation (expected vs actual absence rate): {check.corr().iloc[0, 1]:.3f}")

    print("\n--- Consistency check: overtime_log vs overtime_hours_monthly ---")
    total_hours = overtime_df.groupby("employee_id")["overtime_hours"].sum()
    implied_monthly = (total_hours / (N_DAYS / WORKDAYS_PER_MONTH)).rename("implied_monthly_avg")
    check2 = base_df.set_index("employee_id")[["overtime_hours_monthly"]].join(implied_monthly).fillna(0)
    print(check2.sample(5, random_state=SEED))
    print(f"Correlation (expected vs implied monthly overtime): {check2.corr().iloc[0, 1]:.3f}")

    print("\n--- Consistency check: leave_requests vs leave_utilization_rate ---")
    leave_df = leave_df.assign(
        days=(pd.to_datetime(leave_df["leave_end_date"]) - pd.to_datetime(leave_df["leave_start_date"])).dt.days + 1
    )
    total_leave_days = leave_df.groupby("employee_id")["days"].sum()
    implied_util = (total_leave_days / ANNUAL_LEAVE_ENTITLEMENT_DAYS).rename("implied_utilization_rate")
    check3 = base_df.set_index("employee_id")[["leave_utilization_rate"]].join(implied_util).fillna(0)
    print(check3.sample(5, random_state=SEED))
    print(f"Correlation (expected vs implied leave utilization): {check3.corr().iloc[0, 1]:.3f}")


def main():
    rng = np.random.default_rng(SEED)

    base_df = load_base_employees()
    employees_df = build_employees(base_df)
    attendance_df = build_attendance_events(base_df, rng)
    leave_df = build_leave_requests(base_df, rng)
    overtime_df = build_overtime_log(base_df, rng)

    os.makedirs(DATA_DIR, exist_ok=True)
    employees_df.to_csv(os.path.join(DATA_DIR, "employees.csv"), index=False)
    attendance_df.to_csv(os.path.join(DATA_DIR, "attendance_events.csv"), index=False)
    leave_df.to_csv(os.path.join(DATA_DIR, "leave_requests.csv"), index=False)
    overtime_df.to_csv(os.path.join(DATA_DIR, "overtime_log.csv"), index=False)

    print("Row counts:")
    print(f"  employees.csv:         {len(employees_df)}")
    print(f"  attendance_events.csv: {len(attendance_df)}")
    print(f"  leave_requests.csv:    {len(leave_df)}")
    print(f"  overtime_log.csv:      {len(overtime_df)}")

    print("\nSample rows:")
    for name, df in [
        ("employees.csv", employees_df),
        ("attendance_events.csv", attendance_df),
        ("leave_requests.csv", leave_df),
        ("overtime_log.csv", overtime_df),
    ]:
        print(f"\n{name}:")
        print(df.head(5).to_string(index=False))

    print_consistency_check(base_df, attendance_df, leave_df, overtime_df)


if __name__ == "__main__":
    main()
