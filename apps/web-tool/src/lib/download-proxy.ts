export const DEFAULT_TARGET_ENDPOINT = "http://localhost:8080";

const HOP_BY_HOP_HEADERS = new Set([
	"connection",
	"keep-alive",
	"proxy-authenticate",
	"proxy-authorization",
	"te",
	"trailer",
	"transfer-encoding",
	"upgrade",
	"host",
]);

export function getTargetEndpoint(): string {
	return process.env.BOOKS_API_BASE_URL ?? DEFAULT_TARGET_ENDPOINT;
}

export function createProxyRequest(request: Request, targetEndpoint = getTargetEndpoint()): Request {
	const url = new URL(request.url);
	const headers = new Headers(request.headers);
	for (const header of HOP_BY_HOP_HEADERS) {
		headers.delete(header);
	}

	const init: RequestInit = { method: request.method, headers };
	if (request.method !== "GET" && request.method !== "HEAD") {
		(init as RequestInit & { body?: BodyInit | null; duplex?: "half" }).body = request.body;
		(init as RequestInit & { body?: BodyInit | null; duplex?: "half" }).duplex = "half";
	}

	return new Request(new URL(`${url.pathname}${url.search}`, targetEndpoint), init);
}

export async function proxyDownload(request: Request, fetchImpl: typeof fetch = fetch): Promise<Response> {
	const proxyRequest = createProxyRequest(request);
	return await fetchImpl(proxyRequest);
}
