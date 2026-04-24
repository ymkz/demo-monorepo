import { createFileRoute } from "@tanstack/react-router";
import { z } from "zod";

export const searchParamSchema = z.object({
  isbn: z.string().optional(),
  title: z.string().optional(),
  status: z.string().optional(),
  priceFrom: z.coerce.number().int().optional(),
  priceTo: z.coerce.number().int().optional(),
  publishedAtStart: z.iso.datetime({ local: true, offset: true }).optional(),
  publishedAtEnd: z.iso.datetime({ local: true, offset: true }).optional(),
  order: z.string().default("-published_at"),
  offset: z.coerce.number().int().default(0),
  limit: z.coerce.number().int().default(20),
});

export const Route = createFileRoute("/")({
  validateSearch: (search) => searchParamSchema.parse(search),
  component: IndexPage,
});

export function IndexPage() {
  const query = Route.useSearch();
  const downloadUrl = `/books/download?${new URLSearchParams(
    Object.entries(query)
      .filter(([, v]) => v !== undefined && v !== null)
      .map(([k, v]) => [k, String(v)]),
  )}`;

  return (
    <div className="min-h-screen flex flex-col">
      <header className="bg-white shadow-sm border-b h-16 flex items-center px-6">
        <h1 className="text-xl font-semibold text-gray-900">Web Tool</h1>
      </header>

      <main className="flex-1 p-6 max-w-6xl mx-auto w-full">
        <h2 className="text-2xl font-bold text-gray-900 mb-6">検索パラメータ</h2>

        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <h3 className="text-lg font-semibold text-gray-700 mb-4">パース済みクエリ</h3>
          <pre className="bg-gray-100 rounded p-4 overflow-auto text-sm">{JSON.stringify(query, null, 2)}</pre>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <a
            href={downloadUrl}
            className="inline-flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-label="Download">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"
              />
            </svg>
            ダウンロード
          </a>
        </div>
      </main>

      <footer className="bg-white border-t h-16 flex items-center px-6">
        <p className="text-sm text-gray-600">Footer</p>
      </footer>
    </div>
  );
}
