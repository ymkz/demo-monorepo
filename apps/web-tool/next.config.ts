import type { NextConfig } from "next";

const nextConfig: NextConfig = {
	reactStrictMode: true,
	poweredByHeader: false,
	typescript: { ignoreBuildErrors: true }, // 型チェックは別で実施するため無効にする
};

export default nextConfig;
