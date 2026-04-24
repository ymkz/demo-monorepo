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
