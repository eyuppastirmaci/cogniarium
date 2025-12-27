# Cogniarium

Cogniarium is a privacy-first, self-hosted note-taking application powered by AI. All processing runs entirely on your local machine no external API calls, no data leaves your system.

It allows you to search your notes semantically, generate titles and summaries, and perform sentiment analysis.

## Features

- **Sentiment Analysis:** Analyze the emotional tone of your notes (Positive, Negative, Neutral).

- **Title Generation:** Automatically generate short, context-aware titles for your notes based on their content.

- **Summarize:** Automatically generate concise summaries of your longer notes.

- **Semantic Search:** Search your notes by meaning rather than keyword matching. A search for "cooking attempts" can find notes containing "burnt lasagna recipe" even without exact keyword overlap.

## Technical Details

- PostgreSQL + pgvector for vector embeddings
- Python AI Microservice using `google/flan-t5-base` for title generation and summarization, `cardiffnlp/twitter-roberta-base-sentiment` for sentiment analysis
- All operations run locally within the Docker network
- Optimized models selected for a balance between performance and quality

---

This project is under active development. APIs and features may change.