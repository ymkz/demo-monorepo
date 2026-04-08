import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/")({
	component: IndexPage,
});

export function IndexPage() {
	return (
		<div className="min-h-screen flex flex-col">
			<header className="bg-white shadow-sm border-b h-16 flex items-center px-6">
				<h1 className="text-xl font-semibold">フォーム</h1>
			</header>

			<main className="flex-1 p-6 max-w-6xl mx-auto w-full">
				<h2 className="text-2xl font-bold mb-6">ホーム</h2>
			</main>

			<footer className="bg-white border-t h-16 flex items-center px-6">
				<p className="text-sm">Footer</p>
			</footer>
		</div>
	);
}
