import "./style.css";
import { Spiceflow, serveStatic } from "spiceflow";
import { layout } from "./components/layout";
import { loggerMiddleware } from "./middleware/logger";
import { IndexPage } from "./routes/index";

export const app = new Spiceflow()
	.use(serveStatic({ root: "./public" }))
	.use(loggerMiddleware)
	.layout("/*", layout)
	.page({
		path: "/",
		handler: () => <IndexPage />,
	});
