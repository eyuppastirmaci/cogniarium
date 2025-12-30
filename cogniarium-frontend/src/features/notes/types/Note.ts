export enum SentimentLabel {
  POSITIVE = 'POSITIVE',
  NEGATIVE = 'NEGATIVE',
  NEUTRAL = 'NEUTRAL'
}

export enum NoteUpdateType {
  NOTE_CREATED = 'NOTE_CREATED',
  NOTE_UPDATED = 'NOTE_UPDATED',
  NOTE_DELETED = 'NOTE_DELETED',
  SENTIMENT_UPDATE = 'SENTIMENT_UPDATE',
  TITLE_UPDATE = 'TITLE_UPDATE',
  SUMMARY_UPDATE = 'SUMMARY_UPDATE',
  EMBEDDING_UPDATE = 'EMBEDDING_UPDATE'
}

export interface Note {
    id: number;
    content: string;
    title?: string | null;
    summary?: string | null;
    sentimentLabel?: SentimentLabel | null;
    sentimentScore?: number | null;
    createdAt: string;
}

export interface NoteUpdateMessage {
    type: NoteUpdateType;
    note: Note;
}
  
export interface NoteRequest {
    text: string;
}