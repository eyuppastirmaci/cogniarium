import apiClient from '@/api/axiosClient';
import type { Note, CreateNoteRequest } from '@/features/notes/types/Note';

export const noteService = {
  async getAllNotes(): Promise<Note[]> {
    const response = await apiClient.get<Note[]>('/notes');
    return response.data;
  },

  async createNote(text: string): Promise<Note> {
    const payload: CreateNoteRequest = { text };
    const response = await apiClient.post<Note>('/notes', payload);
    return response.data;
  }
};