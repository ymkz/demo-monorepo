import { renderToStaticMarkup } from "react-dom/server";
import { describe, expect, it } from "vitest";

import { getRouter } from "../router";
import { IndexPage } from "./index";

describe("IndexPage", () => {
	it("Route が / に結び付いていること", () => {
		const router = getRouter();
		expect(router.routesById["/"]).toBeDefined();
	});

	it("フォームとホームが表示されること", () => {
		const html = renderToStaticMarkup(<IndexPage />);

		expect(html).toContain("フォーム");
		expect(html).toContain("ホーム");
	});
});
