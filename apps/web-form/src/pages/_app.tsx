import type { AppProps } from "next/app";
import Head from "next/head";
import { NuqsAdapter } from "nuqs/adapters/next/pages";

export default function CustomApp({ Component, pageProps }: AppProps) {
	return (
		<>
			<Head>
				<meta charSet="utf-8" />
				<meta name="robots" content="noindex" />
				<meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover" />
			</Head>
			<NuqsAdapter>
				<Component {...pageProps} />
			</NuqsAdapter>
		</>
	);
}
