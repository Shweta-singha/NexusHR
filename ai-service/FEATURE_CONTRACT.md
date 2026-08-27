# Attrition Model — Feature Contract

Shared source of truth between `train.py` (Python/sklearn), the FastAPI
Pydantic models in `main.py`, and the Java DTOs in
`org.Employee.dto` / `org.Employee.client.AttritionClient`. If you change a
feature here, update all three.

## Input features

Order matches the `NUMERIC_FEATURES` / `CATEGORICAL_FEATURES` lists in
`train.py`.

| # | Field                        | Type    | Valid range / values                                                        | Description                                             |
|---|-------------------------------|---------|-------------------------------------------------------------------------------|-----------------------------------------------------------|
| 1 | `tenure_years`                | float   | 0–20                                                                           | Years at the company                                     |
| 2 | `role_level`                  | int     | 1–5                                                                            | Seniority band, 1 = most junior, 5 = most senior          |
| 3 | `overtime_hours_monthly`      | float   | 0–60                                                                           | Average overtime hours per month                          |
| 4 | `absence_rate`                | float   | 0.0–1.0                                                                        | Fraction of scheduled working days absent                 |
| 5 | `leave_utilization_rate`      | float   | 0.0–1.0                                                                        | Fraction of allotted leave actually taken                 |
| 6 | `months_since_promotion`      | float   | 0–60                                                                           | Months since the employee's last promotion                |
| 7 | `salary_percentile_in_dept`   | float   | 0–100                                                                          | Employee's salary percentile within their own department  |
| 8 | `job_satisfaction`            | int     | 1–5                                                                            | Self-reported satisfaction score, 1 = lowest, 5 = highest  |
| 9 | `department`                  | string  | `Engineering`, `Sales`, `HR`, `Finance`, `Marketing`, `Operations`             | Employee's department                                     |

## Output

| Field        | Type   | Description                                              |
|--------------|--------|------------------------------------------------------------|
| `risk_score` | float  | Model's predicted probability of attrition, 0.0–1.0        |
| `risk_band`  | string | `Low`, `Medium`, or `High` — bucketed from `risk_score`     |

### Risk band thresholds

| `risk_score` range | `risk_band` |
|---------------------|--------------|
| `< 0.33`             | `Low`        |
| `0.33 – 0.66`        | `Medium`     |
| `> 0.66`             | `High`       |

## Label (training data only, not part of the API)

`attrited` — 0 or 1. **Synthetic**, fabricated by `generate_data.py`'s
signal+noise model. Not derived from any real employee outcome — see that
file's module docstring.
