import { ShirlyReply } from "../types";

function getCurrentTimeText(): string {
  const now = new Date();
  const h = now.getHours().toString().padStart(2, "0");
  const m = now.getMinutes().toString().padStart(2, "0");
  return `השעה עכשיו ${h}:${m}`;
}

function detectIntent(message: string): string {
  const text = message.toLowerCase();

  if (text.includes("מה השעה") || text.includes("שעה עכשיו")) return "TIME";
  if (text.includes("שלום") || text.includes("היי")) return "GREETING";
  if (text.includes("איך קוראים לך")) return "NAME";
  if (text.includes("פתח") && text.includes("יוטיוב")) return "OPEN_YOUTUBE";
  if (text.includes("פתח") && text.includes("כרום")) return "OPEN_CHROME";

  return "SMALL_TALK";
}

export function generateShirlyReply(
  message: string,
  _sessionId: string
): ShirlyReply {
  const intent = detectIntent(message);

  switch (intent) {
    case "TIME":
      return {
        replyText: getCurrentTimeText(),
        actions: [{ type: "NONE" }],
        mood: "neutral",
      };

    case "GREETING":
      return {
        replyText: "שלום, אני שירלי. איך אפשר לעזור?",
        actions: [{ type: "NONE" }],
        mood: "friendly",
      };

    case "NAME":
      return {
        replyText: "קוראים לי שירלי, העוזרת האישית החכמה שלך.",
        actions: [{ type: "NONE" }],
        mood: "friendly",
      };

    case "OPEN_YOUTUBE":
      return {
        replyText: "פותחת לך יוטיוב.",
        actions: [{ type: "OPEN_APP", appId: "youtube" }],
        mood: "neutral",
      };

    case "OPEN_CHROME":
      return {
        replyText: "פותחת את כרום.",
        actions: [{ type: "OPEN_APP", appId: "chrome" }],
        mood: "neutral",
      };

    case "SMALL_TALK":
    default:
      return {
        replyText: `שמעתי שאמרת: "${message}". כרגע אני עדיין לומדת, אבל אפשר ללמד אותי עוד יכולות.`,
        actions: [{ type: "NONE" }],
        mood: "friendly",
      };
  }
}