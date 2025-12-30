import apiClient from '@/api/axiosClient';
import type { Note, NoteRequest } from '@/features/notes/types/Note';

export const noteService = {
  async getAllNotes(): Promise<Note[]> {
    const response = await apiClient.get<Note[]>('/notes');
    return response.data;
  },

  async createNote(text: string): Promise<Note> {
    const payload: NoteRequest = { text };
    const response = await apiClient.post<Note>('/notes', payload);
    return response.data;
  },

  async updateNote(id: number, text: string): Promise<Note> {
    const payload: NoteRequest = { text };
    const response = await apiClient.put<Note>(`/notes/${id}`, payload);
    return response.data;
  },

  async deleteNote(id: number): Promise<void> {
    await apiClient.delete(`/notes/${id}`);
  },

  async searchNotes(query: string): Promise<Note[]> {
    const response = await apiClient.get<Note[]>('/notes/search', {
      params: { q: query }
    });
    return response.data;
  }
};