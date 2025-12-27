from fastapi import FastAPI, BackgroundTasks
from pydantic import BaseModel
from transformers import pipeline
import httpx
from typing import Optional

app = FastAPI()

# Sentiment Analysis Model
SENTIMENT_MODEL_NAME = "cardiffnlp/twitter-roberta-base-sentiment"

# Title Generation Model
TITLE_MODEL_NAME = "google/flan-t5-base"

print(f"Loading sentiment model: {SENTIMENT_MODEL_NAME}...")
sentiment_pipeline = pipeline("sentiment-analysis", model=SENTIMENT_MODEL_NAME)
print("Sentiment model loaded!")

print(f"Loading title generation model: {TITLE_MODEL_NAME}...")
title_pipeline = pipeline("text2text-generation", model=TITLE_MODEL_NAME)
print("Title generation model loaded!")


class SentimentRequest(BaseModel):
    text: str
    callback_url: Optional[str] = None


class TitleRequest(BaseModel):
    text: str
    callback_url: Optional[str] = None


def send_callback(callback_url: str, data: dict):
    """Send callback to backend"""
    print(f"Sending callback to {callback_url} with data: {data}")
    try:
        with httpx.Client(timeout=10.0) as client:
            response = client.post(callback_url, json=data)
            response.raise_for_status()
            print(f"Callback sent successfully to {callback_url}, status: {response.status_code}")
    except Exception as e:
        print(f"Error sending callback to {callback_url}: {e}")
        import traceback
        traceback.print_exc()


def clean_title(title: str) -> str:
    """Clean and format the title"""
    title = ' '.join(title.split())
    title = title.rstrip('.!?:;,')
    
    words = title.split()
    if len(words) > 10:
        title = ' '.join(words[:10])
    
    if len(title) > 80:
        title = title[:77] + "..."
    
    if title:
        title = title[0].upper() + title[1:] if len(title) > 1 else title.upper()
    
    return title


@app.post("/analyze")
def analyze_sentiment(request: SentimentRequest, background_tasks: BackgroundTasks):
    result = sentiment_pipeline(request.text)[0]
    label_map = {"LABEL_0": "NEGATIVE", "LABEL_1": "NEUTRAL", "LABEL_2": "POSITIVE"}
    response_data = {"label": label_map.get(result['label'], result['label']), "score": result['score']}
    
    if request.callback_url:
        background_tasks.add_task(send_callback, request.callback_url, response_data)
        return {"status": "processing", "message": "Sentiment analysis in progress"}
    
    return response_data


@app.post("/generate-title")
def generate_title(request: TitleRequest, background_tasks: BackgroundTasks):
    try:
        text = request.text.strip()
        word_count = len(text.split())
        
        # For very short texts, clean directly
        if word_count <= 5:
            title = clean_title(text)
            response_data = {"title": title}
            if request.callback_url:
                background_tasks.add_task(send_callback, request.callback_url, response_data)
                return {"status": "processing", "message": "Title generation in progress"}
            return response_data
        
        prompt = f"Generate a short title (3-6 words) for this text: {text}"
        
        result = title_pipeline(
            prompt,
            max_new_tokens=20,
            num_return_sequences=1,
            do_sample=False,
            no_repeat_ngram_size=2
        )[0]
        
        title = clean_title(result['generated_text'])
        response_data = {"title": title}
        
        if request.callback_url:
            background_tasks.add_task(send_callback, request.callback_url, response_data)
            return {"status": "processing", "message": "Title generation in progress"}
        
        return response_data
    except Exception as e:
        print(f"Error generating title: {e}")
        import traceback
        traceback.print_exc()
        # In case of error, use the original text as title
        error_title = clean_title(request.text.strip())
        response_data = {"title": error_title}
        if request.callback_url:
            background_tasks.add_task(send_callback, request.callback_url, response_data)
            return {"status": "processing", "message": "Title generation in progress"}
        return response_data