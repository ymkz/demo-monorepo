import type { MiddlewareHandler } from "spiceflow";
import { logger } from "../logger";

const IGNORE_PATH_REGEX = /^\/(favicon|.well-known|healthz).*$/;

export const loggerMiddleware: MiddlewareHandler = async (context, next) => {
	const url = new URL(context.request.url);

	if (IGNORE_PATH_REGEX.test(url.pathname)) return await next();

	const start = performance.now();

	const response = await next();

	const end = performance.now();
	const method = context.request.method.toUpperCase();
	const path = url.pathname;
	const statusCode = response.status;
	const durationMs = (end - start).toFixed(3);

	const message = { method, path, statusCode, durationMs };

	if (statusCode >= 400) {
		logger.error({ ...message }, "response_error");
	} else {
		logger.info({ ...message }, "response_success");
	}
};
