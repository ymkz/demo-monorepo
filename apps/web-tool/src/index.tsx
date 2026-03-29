import "./style.css";
import { Spiceflow, serveStatic } from "spiceflow";
import { layout } from "./components/layout";
import { loggerMiddleware } from "./middleware/logger";
import { booksDownloadHandler } from "./routes/books/download";
import { errorHandler } from "./routes/error";
import { IndexPage, searchParamSchema } from "./routes/index";

export const app = new Spiceflow()
	.use(serveStatic({ root: "./public" }))
	.use(loggerMiddleware)
	.onError(errorHandler)
	.layout("/*", layout)
	.page({
		method: "GET",
		path: "/",
		query: searchParamSchema,
		handler: (context) => <IndexPage query={context.query} />,
	})
	.route({
		method: "GET",
		path: "/books/download",
		handler: (context) => booksDownloadHandler(context.request),
	});
