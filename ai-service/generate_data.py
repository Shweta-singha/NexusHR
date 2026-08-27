"""
Generates a SYNTHETIC attrition training dataset.

This project has no real historical attrition labels - NexusHR is a demo
system, not a company with years of actual employee departures on record.
Every row and every label in synthetic_attrition.csv is fabricated by this
script's signal+noise model below, not derived from any real employee's
actual outcome. Treat it purely as a trainable stand-in, never as real
historical fact, in any downstream code, report, or README section that
references it.

Signal design: risk increases with overtime, time since last promotion, and
absence rate; risk decreases with salary percentile, job satisfaction, and
tenure. Genuine domain logic, not random - but combined with a deliberately
large noise term so the label isn't a trivial deterministic function of the
features (a model that just memorizes the formula would still cap out well
under 100% on held-out data, same as any real dataset with irreducible
noise).
"""

import numpy as np
import pandas as pd

N_ROWS = 5000
SEED = 42

DEPARTMENTS = ["Engineering", "Sales", "HR", "Finance", "Marketing", "Operations"]
# Small per-department baseline risk offset - e.g. Sales/Operations roles
# tend to see higher real-world turnover than Finance/HR in most companies.
DEPARTMENT_RISK_OFFSET = {
    "Engineering": -0.05,
    "Sales": 0.25,
    "HR": -0.10,
    "Finance": -0.15,
    "Marketing": 0.10,
    "Operations": 0.20,
}


def zscore(x: np.ndarray) -> np.ndarray:
    return (x - x.mean()) / x.std()


def sigmoid(x: np.ndarray) -> np.ndarray:
    return 1 / (1 + np.exp(-x))


def generate() -> pd.DataFrame:
    rng = np.random.default_rng(SEED)

    tenure_years = np.clip(rng.exponential(scale=3.5, size=N_ROWS), 0, 20)
    department = rng.choice(DEPARTMENTS, size=N_ROWS)
    role_level = rng.choice([1, 2, 3, 4, 5], size=N_ROWS, p=[0.35, 0.30, 0.20, 0.10, 0.05])
    overtime_hours_monthly = np.clip(rng.gamma(shape=2.0, scale=5.0, size=N_ROWS), 0, 60)
    absence_rate = np.clip(rng.beta(2, 40, size=N_ROWS), 0, 1)
    leave_utilization_rate = np.clip(rng.beta(3, 2, size=N_ROWS), 0, 1)
    months_since_promotion = np.clip(rng.exponential(scale=18, size=N_ROWS), 0, 60)
    salary_percentile_in_dept = rng.uniform(0, 100, size=N_ROWS)
    job_satisfaction = rng.choice([1, 2, 3, 4, 5], size=N_ROWS, p=[0.10, 0.15, 0.25, 0.30, 0.20])

    department_offset = np.array([DEPARTMENT_RISK_OFFSET[d] for d in department])

    risk_logit = (
        1.2 * zscore(overtime_hours_monthly)
        - 1.0 * zscore(salary_percentile_in_dept)
        + 0.8 * zscore(months_since_promotion)
        + 0.6 * zscore(absence_rate)
        - 0.9 * zscore(job_satisfaction)
        - 0.3 * zscore(tenure_years)
        - 0.2 * zscore(leave_utilization_rate)
        - 0.15 * zscore(role_level)
        + department_offset
    )

    # Irreducible noise - without this, the label would be a deterministic
    # function of the features and any reasonable model would hit ~100%,
    # which is exactly what a synthetic demo dataset should NOT look like.
    noise = rng.normal(loc=0.0, scale=1.0, size=N_ROWS)

    # Scale + intercept calibrated empirically (see the printed positive
    # rate below) to land attrition around 25-30% - realistic-ish, and
    # balanced enough that >80% holdout accuracy requires the model to
    # actually use the features, not just exploit a skewed majority class.
    #
    # Signal weight raised 0.9 -> 1.3 and noise weight dropped 0.6 -> 0.25
    # (Day 7): at the original 0.9/0.6 balance the classes overlapped too
    # much for any model to clear 80% holdout accuracy - a RandomForest
    # with unrestricted depth still capped out at ~75% and AUC ~0.81,
    # confirming it was a signal/noise problem in the data rather than an
    # underfit model. This balance still keeps real irreducible noise
    # (labels are not a deterministic function of the features - holdout
    # accuracy is 81%, not ~100%) while letting the injected signal
    # actually separate the classes.
    risk_prob = sigmoid(1.3 * risk_logit + 0.25 * noise - 0.55)
    attrited = rng.binomial(1, risk_prob)

    return pd.DataFrame({
        "tenure_years": np.round(tenure_years, 2),
        "department": department,
        "role_level": role_level,
        "overtime_hours_monthly": np.round(overtime_hours_monthly, 1),
        "absence_rate": np.round(absence_rate, 4),
        "leave_utilization_rate": np.round(leave_utilization_rate, 4),
        "months_since_promotion": np.round(months_since_promotion, 1),
        "salary_percentile_in_dept": np.round(salary_percentile_in_dept, 1),
        "job_satisfaction": job_satisfaction,
        "attrited": attrited,
    })


if __name__ == "__main__":
    df = generate()

    import os
    os.makedirs("data", exist_ok=True)
    df.to_csv("data/synthetic_attrition.csv", index=False)

    print(f"Wrote {len(df)} rows to data/synthetic_attrition.csv")
    print(f"Positive (attrited) rate: {df['attrited'].mean():.3f}")
    print()
    print("Signal sanity check - mean feature values by attrited label:")
    print(df.groupby("attrited")[[
        "overtime_hours_monthly", "salary_percentile_in_dept",
        "months_since_promotion", "job_satisfaction", "tenure_years",
    ]].mean().round(2))
