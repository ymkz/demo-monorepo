import { stringify } from "node:querystring";
import { Writable } from "node:stream";
import type { NextApiRequest, NextApiResponse } from "next";

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
	if (req.method !== "GET") {
		return res.status(405).end();
	}

	const abort = new AbortController();
	req.socket.on("close", () => abort.abort());

	const url = new URL("http://localhost:8080/books/download");
	url.search = stringify(req.query);
	const response = await fetch(url, { method: "GET", headers: { "X-API-Token": "TODO" } });

	res.setHeader("Content-Type", "text/csv; charset=utf-8");
	res.setHeader("Content-Disposition", "attachment; filename=books_YYYYMMDD.csv");
	res.statusCode = response.status;

	if (!response.body) {
		return res.end();
	}

	await response.body.pipeTo(Writable.toWeb(res), { signal: abort.signal });
}

export const config = {
	api: {
		bodyParser: false,
	},
};
