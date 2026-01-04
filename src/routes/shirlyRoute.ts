import { Router } from "express";
import { z } from "zod";
import { generateShirlyReply } from "../services/shirlyService";

const router = Router();

const chatSchema = z.object({
  sessionId: z.string().min(1),
  message: z.string().min(1),
  language: z.string().optional(),
});

router.post("/chat", (req, res) => {
  const parseResult = chatSchema.safeParse(req.body);

  if (!parseResult.success) {
    return res.status(400).json({
      success: false,
      error: "Invalid request",
      details: parseResult.error.flatten(),
    });
  }

  const { sessionId, message } = parseResult.data;
  const reply = generateShirlyReply(message, sessionId);

  return res.json({
    success: true,
    reply,
  });
});

export const shirlyRouter = router;