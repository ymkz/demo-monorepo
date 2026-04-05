import "./style.css";
import { Spiceflow, serveStatic } from "spiceflow";
import { layout } from "./components/layout";
import { IndexPage } from "./routes/index";

export const app = new Spiceflow()
	.use(serveStatic({ root: "./public" }))
	.layout("/*", layout)
	.page({
		path: "/",
		handler: () => <IndexPage />,
	});
