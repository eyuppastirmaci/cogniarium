from fastapi import FastAPI
from pydantic import BaseModel
from transformers import pipeline

app = FastAPI()
MODEL_NAME = "cardiffnlp/twitter-roberta-base-sentiment"

print(f"Loading model: {MODEL_NAME}...")
sentiment_pipeline = pipeline("sentiment-analysis", model=MODEL_NAME)
print("Model loaded!")

class SentimentRequest(BaseModel):
    text: str

@app.post("/analyze")
def analyze_sentiment(request: SentimentRequest):
    result = sentiment_pipeline(request.text)[0]
    label_map = {"LABEL_0": "NEGATIVE", "LABEL_1": "NEUTRAL", "LABEL_2": "POSITIVE"}
    return {"label": label_map.get(result['label'], result['label']), "score": result['score']}