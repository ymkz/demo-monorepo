import { stringify } from "node:querystring";
import type { GetServerSidePropsContext, InferGetServerSidePropsType } from "next";
import { createLoader, parseAsInteger, parseAsIsoDateTime, parseAsString } from "nuqs/server";

const booksSearchParams = {
	isbn: parseAsString,
	title: parseAsString,
	status: parseAsString,
	priceFrom: parseAsInteger,
	priceTo: parseAsInteger,
	publishedAtStart: parseAsIsoDateTime,
	publishedAtEnd: parseAsIsoDateTime,
	// order: parseAsStringLiteral(Object.keys(FindBooksOrder)).withDefault(FindBooksOrder["-published_at"]),
	offset: parseAsInteger.withDefault(0),
	limit: parseAsInteger.withDefault(20),
};
const loadSearchParams = createLoader(booksSearchParams);

export const getServerSideProps = async (context: GetServerSidePropsContext) => {
	const _searchParams = loadSearchParams(context.query);

	return {
		props: {
			searchParams: context.query,
			downloadUrl: `/api/books/download?${stringify(context.query)}`,
		},
	};
};

export default function Page(props: InferGetServerSidePropsType<typeof getServerSideProps>) {
	return (
		<>
			<h1>Page</h1>
			<div>{JSON.stringify(props.searchParams)}</div>
			<a href={props.downloadUrl}>download</a>
		</>
	);
}
