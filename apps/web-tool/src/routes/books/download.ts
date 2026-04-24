import { createFileRoute } from "@tanstack/react-router";

const TARGET_ENDPOINT = "http://localhost:8080";

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
