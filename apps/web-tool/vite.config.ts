import tailwindcss from "@tailwindcss/vite";
import react from "@vitejs/plugin-react";
import { spiceflowPlugin } from "spiceflow/vite";
import { defineConfig } from "vite";

export default defineConfig({
	clearScreen: false,
	server: { port: 4000 },
	plugins: [react(), tailwindcss(), spiceflowPlugin({ entry: "./src/index.tsx" })],
});
