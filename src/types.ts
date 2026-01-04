export interface ShirlyRequestBody {
  sessionId: string;
  message: string;
  language?: string; // "he" / "en" בעתיד
}

export interface ShirlyReply {
  replyText: string;
  actions: ShirlyAction[];
  mood: "friendly" | "neutral" | "serious";
}

export type ShirlyAction =
  | { type: "NONE" }
  | { type: "OPEN_APP"; appId: string }
  | { type: "OPEN_URL"; url: string }
  | { type: "SHOW_NOTIFICATION"; title: string; body: string };