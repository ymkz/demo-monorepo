import type { NextConfig } from "next";

const nextConfig: NextConfig = {
	output: "export",
	distDir: "dist",
	images: { unoptimized: true },
	basePath: process.env.NODE_ENV === "production" ? "/demo-monorepo" : "",
};

export default nextConfig;
