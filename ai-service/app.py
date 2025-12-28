from fastapi import FastAPI, BackgroundTasks
from pydantic import BaseModel
from transformers import pipeline
from sentence_transformers import SentenceTransformer
import httpx
from typing import Optional

app = FastAPI()

# Sentiment Analysis Model
SENTIMENT_MODEL_NAME = "cardiffnlp/twitter-roberta-base-sentiment-latest"

# Title Generation Model
TITLE_MODEL_NAME = "google/flan-t5-base"

# Summarization Model
SUMMARY_MODEL_NAME = "facebook/bart-large-cnn"

# Embedding Model
EMBEDDING_MODEL_NAME = "sentence-transformers/all-MiniLM-L6-v2"

print(f"Loading sentiment model: {SENTIMENT_MODEL_NAME}...")
try:
    sentiment_pipeline = pipeline("sentiment-analysis", model=SENTIMENT_MODEL_NAME)
    print("Sentiment model loaded successfully!")
except Exception as e:
    print(f"ERROR: Failed to load sentiment model: {e}")
    import traceback
    traceback.print_exc()
    raise

print(f"Loading title generation model: {TITLE_MODEL_NAME}...")
try:
    title_pipeline = pipeline("text2text-generation", model=TITLE_MODEL_NAME)
    print("Title generation model loaded successfully!")
except Exception as e:
    print(f"ERROR: Failed to load title generation model: {e}")
    import traceback
    traceback.print_exc()
    raise

print(f"Loading summarization model: {SUMMARY_MODEL_NAME}...")
try:
    summary_pipeline = pipeline("summarization", model=SUMMARY_MODEL_NAME)
    print("Summarization model loaded successfully!")
except Exception as e:
    print(f"ERROR: Failed to load summarization model: {e}")
    import traceback
    traceback.print_exc()
    raise

print(f"Loading embedding model: {EMBEDDING_MODEL_NAME}...")
try:
    embedding_model = SentenceTransformer(EMBEDDING_MODEL_NAME)
    print("Embedding model loaded successfully!")
except Exception as e:
    print(f"ERROR: Failed to load embedding model: {e}")
    import traceback
    traceback.print_exc()
    raise


class SentimentRequest(BaseModel):
    text: str
    callback_url: Optional[str] = None


class TitleRequest(BaseModel):
    text: str
    callback_url: Optional[str] = None


class SummarizeRequest(BaseModel):
    text: str
    callback_url: Optional[str] = None


class EmbeddingRequest(BaseModel):
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
    try:
        result = sentiment_pipeline(request.text)[0]
        label = result.get('label', 'NEUTRAL')
        # Ensure label is a string
        if not isinstance(label, str):
            label = str(label)
        score = result.get('score', 0.0)
        response_data = {"label": label.upper(), "score": float(score)}
        
        if request.callback_url:
            background_tasks.add_task(send_callback, request.callback_url, response_data)
            return {"status": "processing", "message": "Sentiment analysis in progress"}
        
        return response_data
    except Exception as e:
        print(f"Error analyzing sentiment: {e}")
        import traceback
        traceback.print_exc()
        # Return neutral sentiment as fallback
        error_response = {"label": "NEUTRAL", "score": 0.0}
        if request.callback_url:
            background_tasks.add_task(send_callback, request.callback_url, error_response)
            return {"status": "processing", "message": "Sentiment analysis in progress"}
        return error_response


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


@app.post("/summarize")
def summarize(request: SummarizeRequest, background_tasks: BackgroundTasks):
    try:
        text = request.text.strip()
        word_count = len(text.split())
        
        # For very short texts, skip summarization or return a simple summary
        if word_count <= 20:
            # For very short texts, just return the text as summary
            response_data = {"summary": text}
            if request.callback_url:
                background_tasks.add_task(send_callback, request.callback_url, response_data)
                return {"status": "processing", "message": "Summarization in progress"}
            return response_data
        
        # Calculate appropriate max_length and min_length based on text length
        # Bart-large-cnn works best with max_length around 130 for news articles
        # Adjust based on input length: roughly 1/3 of input length, but cap at reasonable values
        text_length = len(text.split())
        max_length = min(150, max(50, text_length // 3))
        min_length = min(30, max(20, text_length // 6))
        
        result = summary_pipeline(
            text,
            max_length=max_length,
            min_length=min_length,
            do_sample=False
        )[0]
        
        summary_text = result['summary_text'].strip()
        response_data = {"summary": summary_text}
        
        if request.callback_url:
            background_tasks.add_task(send_callback, request.callback_url, response_data)
            return {"status": "processing", "message": "Summarization in progress"}
        
        return response_data
    except Exception as e:
        print(f"Error generating summary: {e}")
        import traceback
        traceback.print_exc()
        # In case of error, use first 100 words as fallback summary
        words = request.text.strip().split()
        error_summary = ' '.join(words[:100])
        if len(words) > 100:
            error_summary += "..."
        response_data = {"summary": error_summary}
        if request.callback_url:
            background_tasks.add_task(send_callback, request.callback_url, response_data)
            return {"status": "processing", "message": "Summarization in progress"}
        return response_data


@app.post("/generate-embedding")
def generate_embedding(request: EmbeddingRequest, background_tasks: BackgroundTasks):
    try:
        text = request.text.strip()
        
        if not text:
            raise ValueError("Text cannot be empty")
        
        # Generate embedding using sentence-transformers
        # Returns a numpy array, convert to list of floats
        embedding = embedding_model.encode(text, convert_to_numpy=True)
        embedding_list = embedding.tolist()
        
        response_data = {"embedding": embedding_list}
        
        if request.callback_url:
            background_tasks.add_task(send_callback, request.callback_url, response_data)
            return {"status": "processing", "message": "Embedding generation in progress"}
        
        return response_data
    except Exception as e:
        print(f"Error generating embedding: {e}")
        import traceback
        traceback.print_exc()
        # Return empty embedding as fallback (384 zeros for all-MiniLM-L6-v2)
        error_embedding = [0.0] * 384
        error_response = {"embedding": error_embedding}
        if request.callback_url:
            background_tasks.add_task(send_callback, request.callback_url, error_response)
            return {"status": "processing", "message": "Embedding generation in progress"}
        return error_response