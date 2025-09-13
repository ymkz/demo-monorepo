import { defineConfig } from "orval";

export default defineConfig({
	apifetch: {
		input: {
			target: "../../document/apispec/openapi.json",
		},
		output: {
			target: "./src/generated/openapi/fetch/index.ts",
			schemas: "./src/generated/openapi/schema",
			mode: "single",
			client: "fetch",
			// biome-ignore lint/suspicious/noTemplateCurlyInString: 環境変数の参照を埋め込む
			baseUrl: "${process.env.API_URL}",
			biome: true,
		},
	},
});
