"""
Trains the attrition-risk model on the synthetic dataset (see
generate_data.py's docstring - this is fabricated training data, not real
employee history) and serializes the full pipeline for main.py to load.
"""

import os

import joblib
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score, roc_auc_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder

# Order matches FEATURE_CONTRACT.md - the Pydantic request models in main.py
# and the Java DTO must all agree on this same order and these same names.
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
CATEGORICAL_FEATURES = ["department"]
FEATURES = NUMERIC_FEATURES + CATEGORICAL_FEATURES
LABEL = "attrited"

MODEL_PATH = "model/attrition_model.joblib"


def build_pipeline() -> Pipeline:
    preprocessor = ColumnTransformer(
        transformers=[
            ("department", OneHotEncoder(handle_unknown="ignore"), CATEGORICAL_FEATURES),
        ],
        remainder="passthrough",
    )
    classifier = RandomForestClassifier(
        n_estimators=200,
        max_depth=10,
        min_samples_leaf=8,
        random_state=42,
    )
    return Pipeline(steps=[("preprocess", preprocessor), ("classify", classifier)])


def main():
    df = pd.read_csv("data/synthetic_attrition.csv")

    X_train, X_holdout, y_train, y_holdout = train_test_split(
        df[FEATURES], df[LABEL], test_size=0.2, stratify=df[LABEL], random_state=42,
    )

    pipeline = build_pipeline()
    pipeline.fit(X_train, y_train)

    y_pred = pipeline.predict(X_holdout)
    y_proba = pipeline.predict_proba(X_holdout)[:, 1]

    accuracy = accuracy_score(y_holdout, y_pred)
    auc = roc_auc_score(y_holdout, y_proba)

    print(f"Holdout accuracy: {accuracy:.4f}")
    print(f"Holdout AUC:      {auc:.4f}")

    os.makedirs("model", exist_ok=True)
    joblib.dump(pipeline, MODEL_PATH)
    print(f"Saved pipeline to {MODEL_PATH}")


if __name__ == "__main__":
    main()
