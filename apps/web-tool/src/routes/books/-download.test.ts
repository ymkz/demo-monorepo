import { describe, expect, it, vi } from "vitest";
import { handleBooksDownloadRequest } from "./download";

describe("handleBooksDownloadRequest", () => {
	it("upstream 例外時に 502 を返す", async () => {
		const request = new Request("http://localhost/books/download");
		const fetchImpl = vi.fn(async () => {
			throw new Error("boom");
		});

		const response = await handleBooksDownloadRequest(request, fetchImpl as typeof fetch);

		expect(response.status).toBe(502);
		expect(await response.text()).toBe("Upstream request failed");
	});
});
