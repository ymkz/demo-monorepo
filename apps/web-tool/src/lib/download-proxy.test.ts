import { describe, expect, it, vi } from "vitest";
import { createProxyRequest, proxyDownload } from "./download-proxy";

describe("createProxyRequest", () => {
	it("パス・クエリ・HTTPメソッド・acceptヘッダーを維持し、hop-by-hop ヘッダーを除外する", () => {
		const request = new Request("http://localhost/books/download?foo=bar", {
			method: "POST",
			headers: {
				accept: "application/pdf",
				connection: "keep-alive",
				host: "localhost",
			},
			body: "payload",
		});

		const proxyRequest = createProxyRequest(request, "http://upstream.example");

		expect(proxyRequest.url).toBe("http://upstream.example/books/download?foo=bar");
		expect(proxyRequest.method).toBe("POST");
		expect(proxyRequest.headers.get("accept")).toBe("application/pdf");
		expect(proxyRequest.headers.get("connection")).toBeNull();
		expect(proxyRequest.headers.get("host")).toBeNull();
	});
});

describe("proxyDownload", () => {
	it("upstream request を fetch に渡す", async () => {
		const request = new Request("http://localhost/books/download?foo=bar", {
			method: "GET",
			headers: { accept: "application/pdf" },
		});
		const fetchImpl = vi.fn(async (_input: RequestInfo | URL) => new Response("ok"));

		await proxyDownload(request, fetchImpl as typeof fetch);

		expect(fetchImpl).toHaveBeenCalledTimes(1);
		const [proxyRequest] = fetchImpl.mock.calls[0] as [Request];
		expect(proxyRequest.url).toBe("http://localhost:8080/books/download?foo=bar");
		expect(proxyRequest.method).toBe("GET");
		expect(proxyRequest.headers.get("accept")).toBe("application/pdf");
	});
});
