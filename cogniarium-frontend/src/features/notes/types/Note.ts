export enum SentimentLabel {
  POSITIVE = 'POSITIVE',
  NEGATIVE = 'NEGATIVE',
  NEUTRAL = 'NEUTRAL'
}

export interface Note {
    id: number;
    content: string;
    sentimentLabel: SentimentLabel;
    sentimentScore: number;
    createdAt: string;
}
  
export interface CreateNoteRequest {
    text: string;
}