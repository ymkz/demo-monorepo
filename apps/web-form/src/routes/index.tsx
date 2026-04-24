import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/")({
  component: IndexPage,
});

export function IndexPage() {
  return (
    <div className="flex min-h-screen flex-col bg-slate-50">
      <header className="flex h-16 items-center border-b bg-white px-6 shadow-sm">
        <h1 className="text-xl font-semibold text-slate-950">フォーム</h1>
      </header>

      <main className="mx-auto w-full max-w-4xl flex-1 p-6">
        <section className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
          <p className="text-sm font-semibold text-slate-500">TanStack Start</p>
          <h2 className="mt-1 text-2xl font-bold text-slate-950">ホーム</h2>
          <p className="mt-2 text-sm text-slate-600">
            web-form は TanStack Start の file-based route として描画されています。
          </p>
        </section>
      </main>

      <footer className="flex h-16 items-center border-t bg-white px-6">
        <p className="text-sm text-slate-500">web-form</p>
      </footer>
    </div>
  );
}
