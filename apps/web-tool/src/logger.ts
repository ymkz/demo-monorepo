import { pino } from "pino";
import { Temporal } from "temporal-polyfill-lite";

export const logger = pino({
	level: process.env.LOG_LEVEL || "info",
	formatters: {
		level: (label) => ({ level: label.toUpperCase() }),
		bindings: () => ({}),
	},
	timestamp: () => `,"time":"${Temporal.Now.zonedDateTimeISO().toString({ timeZoneName: "never" })}"`,
	mixin: () => ({ app: "web-tool" }),
});
