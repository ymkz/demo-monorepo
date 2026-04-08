import { z } from "zod";

export const searchParamSchema = z.object({
	isbn: z.string().optional(),
	title: z.string().optional(),
	status: z.string().optional(),
	priceFrom: z.coerce.number().int().optional(),
	priceTo: z.coerce.number().int().optional(),
	publishedAtStart: z.string().datetime({ local: true, offset: true }).optional(),
	publishedAtEnd: z.string().datetime({ local: true, offset: true }).optional(),
	order: z.string().default("-published_at"),
	offset: z.coerce.number().int().default(0),
	limit: z.coerce.number().int().default(20),
});

export type SearchParams = z.infer<typeof searchParamSchema>;

export const parseSearchParams = (search: Record<string, unknown>) => searchParamSchema.parse(search);

export const toDownloadSearchParams = (query: SearchParams) => {
	const params = new URLSearchParams();
	for (const [key, value] of Object.entries(query)) {
		if (value !== undefined && value !== null) params.set(key, String(value));
	}
	return params;
};
