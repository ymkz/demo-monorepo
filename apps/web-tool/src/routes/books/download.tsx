import { createFileRoute } from "@tanstack/react-router";
import { proxyDownload } from "../../lib/download-proxy";

export async function handleBooksDownloadRequest(request: Request, fetchImpl: typeof fetch = fetch): Promise<Response> {
	try {
		return await proxyDownload(request, fetchImpl);
	} catch {
		return new Response("Upstream request failed", { status: 502 });
	}
}

export const Route = createFileRoute("/books/download" as never)({
	server: {
		handlers: {
			GET: async ({ request }) => handleBooksDownloadRequest(request),
		},
	},
});
