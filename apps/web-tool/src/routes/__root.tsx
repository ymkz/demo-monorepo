import { createRootRoute, HeadContent, Outlet, Scripts } from "@tanstack/react-router";
import type { ReactNode } from "react";
import "../style.css";

export const Route = createRootRoute({
  head: () => ({
    meta: [
      { charSet: "utf-8" },
      { name: "robots", content: "noindex" },
      { name: "viewport", content: "width=device-width, initial-scale=1.0, viewport-fit=cover" },
      { title: "Web Tool" },
    ],
  }),
  component: RootComponent,
  notFoundComponent: NotFoundPage,
});

function RootComponent() {
  return (
    <RootDocument>
      <Outlet />
    </RootDocument>
  );
}

function RootDocument({ children }: Readonly<{ children: ReactNode }>) {
  return (
    <html lang="ja">
      <head>
        <HeadContent />
      </head>
      <body className="min-h-screen bg-gray-50">
        {children}
        <Scripts />
      </body>
    </html>
  );
}

function NotFoundPage() {
  return (
    <main className="flex min-h-screen items-center justify-center p-6">
      <div className="rounded-lg bg-white p-6 text-center shadow">
        <h1 className="text-xl font-semibold text-gray-900">ページが見つかりません</h1>
        <p className="mt-2 text-sm text-gray-600">書籍検索画面はトップページから利用できます。</p>
        <a
          className="mt-4 inline-flex h-10 items-center rounded-md bg-gray-900 px-4 text-sm font-medium text-white hover:bg-gray-700"
          href="/"
        >
          書籍検索へ戻る
        </a>
      </div>
    </main>
  );
}
