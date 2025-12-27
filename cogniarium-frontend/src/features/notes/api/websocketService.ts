import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { NoteUpdateMessage } from '@/features/notes/types/Note';
import { apiConfig } from '@/config/apiConfig';

class WebSocketService {
  private client: Client | null = null;
  private isConnected = false;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private onNoteUpdateCallback: ((message: NoteUpdateMessage) => void) | null = null;

  connect() {
    if (this.client && this.isConnected) {
      return;
    }

    // Create SockJS connection
    const socket = new SockJS(apiConfig.webSocketUrl);
    
    this.client = new Client({
      webSocketFactory: () => socket as any,
      debug: (str) => {
        console.log('STOMP:', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        this.isConnected = true;
        this.reconnectAttempts = 0;
        this.subscribe();
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
      },
      onDisconnect: () => {
        this.isConnected = false;
      },
      onWebSocketClose: () => {
        this.isConnected = false;
        this.attemptReconnect();
      },
    });

    this.client.activate();
  }

  private subscribe() {
    if (!this.client || !this.isConnected) {
      return;
    }

    this.client.subscribe('/topic/notes', (message) => {
      try {
        const updateMessage: NoteUpdateMessage = JSON.parse(message.body);
        if (this.onNoteUpdateCallback) {
          this.onNoteUpdateCallback(updateMessage);
        }
      } catch (error) {
        console.error('Error parsing WebSocket message:', error);
      }
    });
  }

  private attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      setTimeout(() => {
        if (!this.isConnected) {
          this.connect();
        }
      }, 5000);
    } else {
      console.error('Max reconnection attempts reached');
    }
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
      this.isConnected = false;
    }
  }

  onNoteUpdate(callback: (message: NoteUpdateMessage) => void) {
    this.onNoteUpdateCallback = callback;
  }
}

export const websocketService = new WebSocketService();

