export enum SentimentLabel {
  POSITIVE = 'POSITIVE',
  NEGATIVE = 'NEGATIVE',
  NEUTRAL = 'NEUTRAL'
}

export enum NoteUpdateType {
  NOTE_CREATED = 'NOTE_CREATED',
  SENTIMENT_UPDATE = 'SENTIMENT_UPDATE',
  TITLE_UPDATE = 'TITLE_UPDATE'
}

export interface Note {
    id: number;
    content: string;
    title?: string | null;
    sentimentLabel?: SentimentLabel | null;
    sentimentScore?: number | null;
    createdAt: string;
}

export interface NoteUpdateMessage {
    type: NoteUpdateType;
    note: Note;
}
  
export interface CreateNoteRequest {
    text: string;
}