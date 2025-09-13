import { defineConfig } from "@hey-api/openapi-ts";

export default defineConfig({
	input: "../api/src/main/resources/static/openapi/openapi.json",
	output: "src/generated",
});
