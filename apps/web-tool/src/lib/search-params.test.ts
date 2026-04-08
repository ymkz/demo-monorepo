import { describe, expect, it } from "vitest";
import { parseSearchParams, toDownloadSearchParams } from "./search-params";

describe("search-params", () => {
	it("デフォルト値を補完する", () => {
		expect(parseSearchParams({})).toEqual({ order: "-published_at", offset: 0, limit: 20 });
	});

	it("download query に変換する", () => {
		expect(
			toDownloadSearchParams({
				isbn: "978",
				order: "title",
				offset: 10,
				limit: 5,
			}).toString(),
		).toBe("isbn=978&order=title&offset=10&limit=5");
	});
});
