import { createMDX } from "fumadocs-mdx/next";
import type { NextConfig } from "next";

const nextConfig: NextConfig = {
	output: "export",
	distDir: "dist",
	images: { unoptimized: true },
	basePath: process.env.NODE_ENV === "production" ? "/demo-monorepo" : "",
};

const withMDX = createMDX();

export default withMDX(nextConfig);
