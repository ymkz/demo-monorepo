import { redirect } from "next/navigation";

export default function HomePage() {
	const basePath = process.env.NODE_ENV === "production" ? "/demo-monorepo" : "";
	redirect(`${basePath}/docs`);
}
