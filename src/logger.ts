export enum LogLevel {
  INFO = "INFO",
  ERROR = "ERROR",
}

function log(level: LogLevel, message: string, meta?: unknown) {
  const base = `[${new Date().toISOString()}] [${level}] ${message}`;
  if (meta) {
    // לא מטורף – פשוט מדפיס JSON
    console.log(base, JSON.stringify(meta));
  } else {
    console.log(base);
  }
}

export const logger = {
  info: (message: string, meta?: unknown) => log(LogLevel.INFO, message, meta),
  error: (message: string, meta?: unknown) => log(LogLevel.ERROR, message, meta),
};