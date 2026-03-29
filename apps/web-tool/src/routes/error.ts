import type { ErrorHandler } from "spiceflow/dist/types";

export const errorHandler: ErrorHandler = async (context) => {
	console.error("Error occurred:", context.error);
	return new Response("An error occurred", { status: 500 });
};
