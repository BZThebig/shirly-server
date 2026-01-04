import express from "express";
import cors from "cors";
import helmet from "helmet";
import { config } from "./config";
import { logger } from "./logger";
import { errorHandler } from "./middleware/errorHandler";
import { notFoundHandler } from "./middleware/notFoundHandler";
import { requestLogger } from "./middleware/requestLogger";
import { healthRouter } from "./routes/healthRoute";
import { shirlyRouter } from "./routes/shirlyRoute";

const app = express();

app.use(helmet());
app.use(cors());
app.use(express.json());
app.use(requestLogger);

app.use("/api/v1", healthRouter);
app.use("/api/v1/shirly", shirlyRouter);

app.use(notFoundHandler);
app.use(errorHandler);

app.listen(config.port, () => {
  logger.info(`Shirly API server is running on port ${config.port}`, {
    env: config.nodeEnv,
  });
});