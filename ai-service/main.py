"""
See FEATURE_CONTRACT.md for the request/response schema this API implements
- EmployeeFeatures and the risk band thresholds below must stay in sync with
that doc.
"""

from contextlib import asynccontextmanager
from enum import Enum
from typing import Literal

import joblib
import pandas as pd
from fastapi import FastAPI
from pydantic import BaseModel, Field

MODEL_PATH = "model/attrition_model.joblib"

# Order must match train.py's FEATURES list (department last) so the
# DataFrame built from a request lines up with what the pipeline was fit on.
FEATURE_ORDER = [
    "tenure_years",
    "role_level",
    "overtime_hours_monthly",
    "absence_rate",
    "leave_utilization_rate",
    "months_since_promotion",
    "salary_percentile_in_dept",
    "job_satisfaction",
    "department",
]

model_state: dict = {}


@asynccontextmanager
async def lifespan(app: FastAPI):
    model_state["model"] = joblib.load(MODEL_PATH)
    yield
    model_state.clear()


app = FastAPI(title="NexusHR AI Service", lifespan=lifespan)


@app.get("/health")
def health():
    return {"status": "ok"}


class Department(str, Enum):
    ENGINEERING = "Engineering"
    SALES = "Sales"
    HR = "HR"
    FINANCE = "Finance"
    MARKETING = "Marketing"
    OPERATIONS = "Operations"


class EmployeeFeatures(BaseModel):
    tenure_years: float = Field(ge=0, le=20)
    role_level: int = Field(ge=1, le=5)
    overtime_hours_monthly: float = Field(ge=0, le=60)
    absence_rate: float = Field(ge=0.0, le=1.0)
    leave_utilization_rate: float = Field(ge=0.0, le=1.0)
    months_since_promotion: float = Field(ge=0, le=60)
    salary_percentile_in_dept: float = Field(ge=0, le=100)
    job_satisfaction: int = Field(ge=1, le=5)
    department: Department


class PredictionResponse(BaseModel):
    risk_score: float
    risk_band: Literal["Low", "Medium", "High"]


def risk_band_for(score: float) -> str:
    if score < 0.33:
        return "Low"
    if score <= 0.66:
        return "Medium"
    return "High"


def predict_scores(employees: list[EmployeeFeatures]) -> list[PredictionResponse]:
    df = pd.DataFrame(
        [{field: getattr(e, field) for field in FEATURE_ORDER} for e in employees]
    )
    # Enum -> plain string, since that's what the pipeline's OneHotEncoder
    # was fit on.
    df["department"] = df["department"].apply(lambda d: d.value)

    scores = model_state["model"].predict_proba(df)[:, 1]
    return [
        PredictionResponse(risk_score=round(float(s), 4), risk_band=risk_band_for(s))
        for s in scores
    ]


@app.post("/predict", response_model=PredictionResponse)
def predict(employee: EmployeeFeatures):
    return predict_scores([employee])[0]


@app.post("/predict/batch", response_model=list[PredictionResponse])
def predict_batch(employees: list[EmployeeFeatures]):
    return predict_scores(employees)
