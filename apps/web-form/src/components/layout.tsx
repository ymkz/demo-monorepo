import type { ReactNode } from "react";

interface LayoutProps {
	children?: ReactNode;
}

export const layout = async ({ children }: LayoutProps) => {
	return (
		<html lang="ja">
			<head>
				<meta charSet="utf-8" />
				<meta name="robots" content="noindex" />
				<meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover" />
				<title>フォーム</title>
			</head>
			<body className="min-h-screen">{children}</body>
		</html>
	);
};
