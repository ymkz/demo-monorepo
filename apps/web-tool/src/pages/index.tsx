import { stringify } from "node:querystring";
import { AppShell } from "@mantine/core";
import type { GetServerSidePropsContext, InferGetServerSidePropsType } from "next";
import { createLoader, parseAsInteger, parseAsIsoDateTime, parseAsString } from "nuqs/server";

const searchParams = {
	isbn: parseAsString,
	title: parseAsString,
	status: parseAsString,
	priceFrom: parseAsInteger,
	priceTo: parseAsInteger,
	publishedAtStart: parseAsIsoDateTime,
	publishedAtEnd: parseAsIsoDateTime,
	order: parseAsString.withDefault("-published_at"),
	offset: parseAsInteger.withDefault(0),
	limit: parseAsInteger.withDefault(20),
};
const loadSearchParams = createLoader(searchParams);

export const getServerSideProps = async (context: GetServerSidePropsContext) => {
	const originalQuery = context.query;
	const parsedQuery = loadSearchParams(context.query);

	return {
		props: {
			originalQuery,
			parsedQuery,
			downloadUrl: `/api/books/download?${stringify(context.query)}`,
		},
	};
};

export default function Page(props: InferGetServerSidePropsType<typeof getServerSideProps>) {
	return (
		<AppShell padding="md" header={{ height: 60 }} footer={{ height: 60 }}>
			<AppShell.Header>
				<div>Header</div>
			</AppShell.Header>
			<AppShell.Main>
				<h1>Page</h1>
				<div>{JSON.stringify(props.originalQuery)}</div>
				<div>{JSON.stringify(props.parsedQuery)}</div>
				<a href={props.downloadUrl}>download</a>
			</AppShell.Main>
			<AppShell.Footer>
				<div>Footer</div>
			</AppShell.Footer>
		</AppShell>
	);
}
