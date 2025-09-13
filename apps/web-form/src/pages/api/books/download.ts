import { createProxyServer } from "httpxy";
import type { NextApiRequest, NextApiResponse } from "next";

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
	new Promise((resolve, reject) => {
		createProxyServer()
			.once("proxyReq", async (proxyReq) => {
				proxyReq.setHeader("X-Api-Token", "token");
				proxyReq.path = req.url?.replace("/api", "");
			})
			.once("proxyRes", resolve)
			.once("error", reject)
			.web(req, res, { changeOrigin: true, target: "http://localhost:8080" });
	});
}

export const config = {
	api: {
		externalResolver: true,
		bodyParser: false,
	},
};
