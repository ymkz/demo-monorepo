import { ColorSchemeScript, mantineHtmlProps } from "@mantine/core";
import { Head, Html, Main, NextScript } from "next/document";

export default function CustomDocument() {
	return (
		<Html lang="ja" {...mantineHtmlProps}>
			<Head>
				<ColorSchemeScript />
			</Head>
			<body>
				<Main />
				<NextScript />
			</body>
		</Html>
	);
}
