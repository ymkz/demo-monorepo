import "./global.css";

import type { Metadata } from "next";

export const metadata: Metadata = {
	title: "ツール",
	description: "ツール",
};

export default function Layout({ children }: Readonly<{ children: React.ReactNode }>) {
	return (
		<html lang="ja">
			<body>{children}</body>
		</html>
	);
}
