import './globals.css'
import { Spiceflow, serveStatic } from 'spiceflow'
import { Head, ProgressBar } from 'spiceflow/react'
import { Suspense } from 'react'
import { z } from 'zod'

const searchParamsSchema = z.object({
  isbn: z.string().optional(),
  title: z.string().optional(),
  status: z.string().optional(),
  priceFrom: z.coerce.number().optional(),
  priceTo: z.coerce.number().optional(),
  publishedAtStart: z.string().optional(),
  publishedAtEnd: z.string().optional(),
  order: z.string().default('-published_at'),
  offset: z.coerce.number().default(0),
  limit: z.coerce.number().default(20),
})

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080'

export const app = new Spiceflow()
  .use(serveStatic({ root: './public' }))
  .get('/api/books/download', async ({ request }) => {
    const url = new URL('/books/download', API_BASE_URL)
    url.search = new URL(request.url).search
    const response = await fetch(url, {
      headers: { 'X-API-Token': process.env.API_TOKEN ?? '' },
    })
    return new Response(response.body, {
      status: response.status,
      headers: {
        'Content-Type': 'text/csv; charset=utf-8',
        'Content-Disposition': 'attachment; filename=books_YYYYMMDD.csv',
      },
    })
  })
  .layout('/*', async ({ children }) => {
    return (
      <html lang="ja">
        <Head>
          <meta charSet="utf-8" />
          <meta name="robots" content="noindex" />
          <meta
            name="viewport"
            content="width=device-width, initial-scale=1.0, viewport-fit=cover"
          />
          <Head.Title>Web Tool</Head.Title>
        </Head>
        <body className="min-h-screen bg-gray-50">
          <ProgressBar />
          {children}
        </body>
      </html>
    )
  })
  .page('/', async function Home({ request }) {
    const searchParams = new URL(request.url).searchParams
    const parsed = searchParamsSchema.safeParse(
      Object.fromEntries(searchParams),
    )
    const params = parsed.success ? parsed.data : searchParamsSchema.parse({})
    const downloadUrl = `/api/books/download?${searchParams.toString()}`

    return (
      <div className="flex min-h-screen flex-col">
        <header className="flex h-[60px] items-center border-b bg-white px-4">
          <div>Header</div>
        </header>
        <main className="flex-1 p-4">
          <h1 className="text-2xl font-bold">Page</h1>
          <pre className="mt-4 rounded bg-gray-100 p-4 text-sm">
            {JSON.stringify(params, null, 2)}
          </pre>
          <a
            href={downloadUrl}
            className="mt-4 inline-block text-blue-600 underline"
          >
            download
          </a>
        </main>
        <footer className="flex h-[60px] items-center border-t bg-white px-4">
          <div>Footer</div>
        </footer>
      </div>
    )
  })

app.listen(Number(process.env.PORT || 4000))
