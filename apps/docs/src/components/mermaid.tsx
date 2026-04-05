"use client";

import mermaid from "mermaid";
import { useEffect, useRef, useState } from "react";

interface MermaidProps {
	chart: string;
	caption?: string;
}

export function Mermaid({ chart, caption }: MermaidProps) {
	const ref = useRef<HTMLDivElement>(null);
	const [svg, setSvg] = useState<string>("");
	const [error, setError] = useState<string | null>(null);

	useEffect(() => {
		mermaid.initialize({
			startOnLoad: false,
			theme: "default",
			securityLevel: "strict",
		});

		const renderChart = async () => {
			try {
				const id = `mermaid-${Math.random().toString(36).substr(2, 9)}`;
				const { svg } = await mermaid.render(id, chart.trim());
				setSvg(svg);
				setError(null);
			} catch (err) {
				setError(err instanceof Error ? err.message : "Failed to render chart");
			}
		};

		renderChart();
	}, [chart]);

	if (error) {
		return (
			<div className="p-4 bg-red-50 border border-red-200 rounded-lg">
				<p className="text-red-600 text-sm">Failed to render Mermaid chart:</p>
				<pre className="text-red-500 text-xs mt-2">{error}</pre>
			</div>
		);
	}

	return (
		<figure className="my-6">
			<div
				ref={ref}
				className="overflow-x-auto bg-muted p-4 rounded-lg flex justify-center"
				dangerouslySetInnerHTML={{ __html: svg }}
			/>
			{caption && <figcaption className="text-center text-sm text-muted-foreground mt-2">{caption}</figcaption>}
		</figure>
	);
}
