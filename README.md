# Cogniarium

Cogniarium is a privacy-first, self-hosted note-taking application powered by AI. All processing runs entirely on your local machine no external API calls, no data leaves your system.

It allows you to search your notes semantically, generate titles and summaries, and perform sentiment analysis.

---

![Cogniarium Example](.github/assets/example.png)

---

## Features

- 🔐 **Authentication & Authorization:** Secure user authentication with email verification, JWT tokens, and session management.

- 📊 **Sentiment Analysis:** Analyze the emotional tone of your notes (Positive, Negative, Neutral).

- ✏️ **Title Generation:** Automatically generate short, context-aware titles for your notes based on their content.

- 📄 **Summarize:** Automatically generate concise summaries of your longer notes.

- 🔍 **Semantic Search:** Search your notes by meaning rather than keyword matching. A search for "cooking attempts" can find notes containing "burnt lasagna recipe" even without exact keyword overlap.

## Technical Details

- **Backend:** Spring Boot (Kotlin) with JWT authentication
- **Frontend:** Vue.js with TypeScript
- **Database:** PostgreSQL + pgvector for vector embeddings
- **Caching & Rate Limiting:** Redis for email rate limiting and caching
- **Email:** MailHog for development email testing
- **AI Microservice:** Python service using:
  - `google/flan-t5-base` for title generation
  - `facebook/bart-large-cnn` for summarization
  - `cardiffnlp/twitter-roberta-base-sentiment-latest` for sentiment analysis
  - `sentence-transformers/all-MiniLM-L6-v2` for semantic search embeddings
- All operations run locally within the Docker network
- Optimized models selected for a balance between performance and quality

## Getting Started

### Option 1: Run Everything with Docker

Start all services (database, AI service, backend, and frontend) with Docker:

```bash
docker compose --profile full up --build -d
```

Access the application at:
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **AI Service**: http://localhost:8000
- **Database**: localhost:5433
- **Redis**: localhost:6379
- **MailHog Web UI**: http://localhost:8025 (for viewing emails in development)

### Option 2: Run Only Infrastructure Services with Docker

Start only the infrastructure services (database, AI service, Redis, and MailHog) with Docker, then run backend and frontend locally:

```bash
# Start infrastructure services (db, ai-service, redis, mailhog)
docker compose --profile base up -d

# Run backend locally (in cogniariumbackend directory)
./gradlew bootRun

# Run frontend locally (in cogniarium-frontend directory)
npm run dev
```

This setup is useful for development:
- **Database**: Available at `localhost:5433`
- **AI Service**: Available at `http://localhost:8000`
- **Redis**: Available at `localhost:6379`
- **MailHog**: Available at `localhost:1025` (SMTP) and http://localhost:8025 (Web UI)
- **Backend**: Runs locally on `http://localhost:8080` (connects to Docker services)
- **Frontend**: Runs locally on `http://localhost:5173` (Vite dev server)

### Stopping Services

To stop all services (including frontend and backend containers):

```bash
docker compose --profile full down
```

To stop only infrastructure services (database, AI service, Redis, and MailHog):

```bash
docker compose --profile base down
```

To stop all services and remove volumes (⚠️ **Warning:** This will delete all data):

```bash
docker compose --profile base down -v
```

## Email & Development

### MailHog for Email Testing

In development, Cogniarium uses **MailHog** to capture and display emails instead of sending real emails. This is perfect for testing email verification and other email features.

**Access MailHog:**
- **Web UI**: http://localhost:8025
- **SMTP Port**: `localhost:1025`

When you register a new user or request a verification email:
1. The email is sent to MailHog (not to a real email address)
2. Open http://localhost:8025 in your browser
3. You'll see all captured emails in the MailHog inbox
4. Click on any email to view its contents and copy verification links

**Starting MailHog separately:**

If you only need MailHog (without other services):

```bash
docker compose --profile mail up -d mailhog
```

**Email Rate Limiting:**

Verification emails are rate-limited to prevent abuse:
- Maximum **5 emails per 24 hours** per email address (Smarter limiting might be added in the future lol)
- Rate limits are stored in Redis with automatic expiration (TTL)
- After 24 hours, the limit resets automatically

## Planned Features

- [x] **Note Editing & Deletion:** Edit and delete existing notes
- [x] **Authentication & Authorization:** User management, login/logout, JWT authentication, and email verification
- [ ] **Pagination:** Implement pagination for note lists to improve performance and scalability (currently all notes are loaded at once)
- [ ] **Error Handling & User Feedback:** Toast notifications, error messages, and better loading states for improved UX
- [ ] **Note Organization:** Tags/categories, folders/collections, and favorites/bookmarks for better note management
- [ ] **Filtering & Sorting:** Filter by sentiment (Positive/Negative/Neutral), date range, and various sorting options
- [ ] **Export/Import:** JSON/CSV export functionality and backup/restore capabilities for data portability
- [ ] **Note Detail View:** Full note detail page for viewing complete content (currently only list view exists)
- [ ] **Rich Text Editing:** Markdown support, formatting options, and link support (currently only plain text)
- [ ] **Settings & Preferences:** User-configurable similarity threshold, theme preferences, and search preferences
- [ ] **Caching:** Cache search query embeddings and note lists for improved performance
- [x] **Rate Limiting:** Email rate limiting implemented with Redis (5 emails per 24 hours)
- [ ] **Retry Mechanism:** Retry logic for failed callbacks to ensure reliability
- [ ] **Analytics & Stats:** Note statistics (total count, sentiment distribution) and search analytics
- [ ] **Redis Queue:** Distributed queue system (similar to Celery) for handling embedding generation requests in production environments
