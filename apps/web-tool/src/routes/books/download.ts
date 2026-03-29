import type { SpiceflowRequest } from "spiceflow";

const TARGET_ENDPOINT = "http://localhost:8080";

export const booksDownloadHandler = async (request: SpiceflowRequest) => {
	const url = new URL(request.url);
	const req = new Request(new URL(url.pathname + url.search, TARGET_ENDPOINT), request);
	const res = await fetch(req);
	return res;
};
