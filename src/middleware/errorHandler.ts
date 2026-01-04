import { NextFunction, Request, Response } from "express";
import { logger } from "../logger";

export function errorHandler(
  err: unknown,
  req: Request,
  res: Response,
  _next: NextFunction
) {
  logger.error("Unhandled error", { path: req.path, error: err });

  res.status(500).json({
    success: false,
    error: "Internal server error",
  });
}