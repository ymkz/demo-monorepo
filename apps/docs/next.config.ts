import { createMDX } from "fumadocs-mdx/next";
import type { NextConfig } from "next";

const isProd = process.env.NODE_ENV === "production";

const nextConfig: NextConfig = {
	output: "export",
	distDir: "dist",
	images: { unoptimized: true },
	basePath: isProd ? "/demo-monorepo" : "",
	assetPrefix: isProd ? "/demo-monorepo" : "",
};

const withMDX = createMDX();

export default withMDX(nextConfig);
