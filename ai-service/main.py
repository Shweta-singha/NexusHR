from fastapi import FastAPI

app = FastAPI(title="NexusHR AI Service")


@app.get("/health")
def health():
    return {"status": "ok"}
