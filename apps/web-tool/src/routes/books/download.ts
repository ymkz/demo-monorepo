import { createFileRoute } from "@tanstack/react-router";

const TARGET_ENDPOINT = import.meta.env.VITE_WEB_TOOL_API_BASE_URL ?? "http://localhost:8080";

export const Route = createFileRoute("/books/download")({
  server: {
    handlers: {
      GET: async ({ request }) => {
        const url = new URL(request.url);
        const req = new Request(new URL(url.pathname + url.search, TARGET_ENDPOINT), request);
        return await fetch(req);
      },
    },
  },
});
