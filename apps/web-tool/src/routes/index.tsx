import { createFileRoute } from "@tanstack/react-router";
import { createServerFn } from "@tanstack/react-start";
import { useEffect, useMemo, useState } from "react";
import { z } from "zod";
import { createClient } from "../generated/client";
import { searchBooks } from "../generated/sdk.gen";
import type { ErrorResponse, SearchBooksData, SearchBooksResponse } from "../generated/types.gen";

const TARGET_ENDPOINT = import.meta.env.VITE_WEB_TOOL_API_BASE_URL ?? "http://localhost:8080";

const serverApiClient = createClient({
  baseUrl: TARGET_ENDPOINT,
});

const bookStatuses = ["UNPUBLISHED", "PUBLISHED", "OUT_OF_PRINT"] as const;
const bookStatusLabels: Record<(typeof bookStatuses)[number], string> = {
  UNPUBLISHED: "未出版",
  PUBLISHED: "出版済み",
  OUT_OF_PRINT: "絶版",
};

const orderOptions = [
  { value: "-published_at", label: "出版日時が新しい順" },
  { value: "+published_at", label: "出版日時が古い順" },
  { value: "-price", label: "価格が高い順" },
  { value: "+price", label: "価格が安い順" },
] as const;

const limitOptions = [10, 20, 50, 100] as const;

const optionalString = z.preprocess((value) => {
  const normalizedValue = Array.isArray(value) ? value[0] : value;
  return normalizedValue === "" ? undefined : normalizedValue;
}, z.string().optional());

const optionalNumber = z.preprocess((value) => {
  const normalizedValue = Array.isArray(value) ? value[0] : value;
  return normalizedValue === "" || normalizedValue === undefined ? undefined : normalizedValue;
}, z.coerce.number().int().optional());

const requiredNumber = (defaultValue: number) =>
  z.preprocess((value) => {
    const normalizedValue = Array.isArray(value) ? value[0] : value;
    return normalizedValue === "" || normalizedValue === undefined ? defaultValue : normalizedValue;
  }, z.coerce.number().int().default(defaultValue));

const searchedParam = z.preprocess((value) => {
  const normalizedValue = Array.isArray(value) ? value[0] : value;
  return normalizedValue === "true" || normalizedValue === true;
}, z.boolean().default(false));

export const searchParamSchema = z.object({
  searched: searchedParam,
  isbn: optionalString,
  title: optionalString,
  status: z.preprocess((value) => {
    const normalizedValue = Array.isArray(value) ? value[0] : value;
    return normalizedValue === "" ? undefined : normalizedValue;
  }, z.enum(bookStatuses).optional()),
  priceFrom: optionalNumber.pipe(z.number().int().min(0).optional()),
  priceTo: optionalNumber.pipe(z.number().int().min(0).optional()),
  publishedAtStart: optionalString,
  publishedAtEnd: optionalString,
  order: z.preprocess(
    (value) => {
      const normalizedValue = Array.isArray(value) ? value[0] : value;
      return normalizedValue === "" || normalizedValue === undefined ? "-published_at" : normalizedValue;
    },
    z.enum(["+price", "-price", "+published_at", "-published_at"]).default("-published_at"),
  ),
  offset: requiredNumber(0).pipe(z.number().int().min(0)),
  limit: requiredNumber(20).pipe(z.number().int().min(1).max(100)),
});

type SearchParams = z.infer<typeof searchParamSchema>;
type SearchBooksQuery = NonNullable<SearchBooksData["query"]>;
type SearchBooksResult = {
  data: SearchBooksResponse | null;
  errorMessage: string | null;
};

const searchBooksOnServer = createServerFn({ method: "GET" })
  .inputValidator((query: SearchBooksQuery) => query)
  .handler(async ({ data }) => {
    const response = await searchBooks({
      client: serverApiClient,
      query: data,
    });

    if (response.error) {
      return {
        data: null,
        errorMessage: formatError(response.error),
      } satisfies SearchBooksResult;
    }

    return {
      data: response.data ?? null,
      errorMessage: null,
    } satisfies SearchBooksResult;
  });

export const Route = createFileRoute("/")({
  validateSearch: (search) => searchParamSchema.parse(search),
  component: IndexPage,
});

function IndexPage() {
  const query = Route.useSearch();
  const navigate = Route.useNavigate();
  const [result, setResult] = useState<SearchBooksResponse | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const apiQuery = useMemo(() => toApiQuery(query), [query]);
  const downloadUrl = `/books/download?${new URLSearchParams(
    Object.entries(toDownloadQuery(query))
      .filter(([, value]) => value !== undefined && value !== null)
      .map(([key, value]) => [key, String(value)]),
  )}`;
  const items = result?.items ?? [];
  const pagination = result?.pagination;
  const currentPage = Math.floor(query.offset / query.limit) + 1;
  const totalPages = pagination?.totalPages ?? (items.length > 0 ? currentPage : 0);
  const totalCount = pagination?.totalCount ?? items.length;
  const returnedCount = pagination?.returnedCount ?? items.length;
  const canGoPrevious = query.offset > 0;
  const canGoNext = pagination ? query.offset + returnedCount < totalCount : returnedCount >= query.limit;

  useEffect(() => {
    if (!query.searched) {
      setResult(null);
      setErrorMessage(null);
      setIsLoading(false);
      return;
    }

    const abortController = new AbortController();
    setIsLoading(true);
    setErrorMessage(null);

    searchBooksOnServer({
      data: apiQuery,
      signal: abortController.signal,
    })
      .then((response) => {
        if (abortController.signal.aborted) {
          return;
        }

        if (response.errorMessage) {
          setResult(null);
          setErrorMessage(response.errorMessage);
          return;
        }

        setResult(response.data);
      })
      .catch((error: unknown) => {
        if (abortController.signal.aborted) {
          return;
        }

        setResult(null);
        setErrorMessage(error instanceof Error ? error.message : "書籍検索に失敗しました。");
      })
      .finally(() => {
        if (!abortController.signal.aborted) {
          setIsLoading(false);
        }
      });

    return () => abortController.abort();
  }, [apiQuery, query.searched]);

  return (
    <div className="min-h-screen flex flex-col">
      <header className="bg-white shadow-sm border-b h-16 flex items-center px-6">
        <h1 className="text-xl font-semibold text-gray-900">書籍検索ツール</h1>
      </header>

      <main className="flex-1 p-6 max-w-6xl mx-auto w-full">
        <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">書籍検索</h2>
            <p className="mt-1 text-sm text-gray-600">条件を指定して書籍を検索し、同じ条件で CSV を出力できます。</p>
          </div>
          <a
            href={downloadUrl}
            className="inline-flex h-10 items-center justify-center rounded-md bg-blue-600 px-4 text-sm font-medium text-white transition-colors hover:bg-blue-700"
          >
            <svg className="mr-2 h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-label="Download">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"
              />
            </svg>
            ダウンロード
          </a>
        </div>

        <form
          key={getSearchFormKey(query)}
          className="mb-6 rounded-lg bg-white p-6 shadow"
          onSubmit={(event) => {
            event.preventDefault();
            const nextSearch = getSearchFromForm(new FormData(event.currentTarget), query);
            void navigate({ search: nextSearch });
          }}
        >
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <label className="flex flex-col gap-1 text-sm font-medium text-gray-700">
              ISBN
              <input
                className="h-10 rounded-md border border-gray-300 px-3 text-gray-900 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100"
                name="isbn"
                placeholder="9784873115658"
                type="search"
                defaultValue={query.isbn ?? ""}
              />
            </label>
            <label className="flex flex-col gap-1 text-sm font-medium text-gray-700">
              タイトル
              <input
                className="h-10 rounded-md border border-gray-300 px-3 text-gray-900 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100"
                name="title"
                placeholder="リーダブルコード"
                type="search"
                defaultValue={query.title ?? ""}
              />
            </label>
            <label className="flex flex-col gap-1 text-sm font-medium text-gray-700">
              ステータス
              <select
                className="h-10 rounded-md border border-gray-300 px-3 text-gray-900 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100"
                name="status"
                defaultValue={query.status ?? ""}
              >
                <option value="">すべて</option>
                {bookStatuses.map((status) => (
                  <option key={status} value={status}>
                    {bookStatusLabels[status]}
                  </option>
                ))}
              </select>
            </label>
            <label className="flex flex-col gap-1 text-sm font-medium text-gray-700">
              並び順
              <select
                className="h-10 rounded-md border border-gray-300 px-3 text-gray-900 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100"
                name="order"
                defaultValue={query.order}
              >
                {orderOptions.map((order) => (
                  <option key={order.value} value={order.value}>
                    {order.label}
                  </option>
                ))}
              </select>
            </label>
            <label className="flex flex-col gap-1 text-sm font-medium text-gray-700">
              価格 下限
              <input
                className="h-10 rounded-md border border-gray-300 px-3 text-gray-900 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100"
                min="0"
                name="priceFrom"
                type="number"
                defaultValue={query.priceFrom ?? ""}
              />
            </label>
            <label className="flex flex-col gap-1 text-sm font-medium text-gray-700">
              価格 上限
              <input
                className="h-10 rounded-md border border-gray-300 px-3 text-gray-900 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100"
                min="0"
                name="priceTo"
                type="number"
                defaultValue={query.priceTo ?? ""}
              />
            </label>
            <label className="flex flex-col gap-1 text-sm font-medium text-gray-700">
              出版日時 開始
              <input
                className="h-10 rounded-md border border-gray-300 px-3 text-gray-900 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100"
                name="publishedAtStart"
                type="datetime-local"
                defaultValue={toDateTimeLocalValue(query.publishedAtStart)}
              />
            </label>
            <label className="flex flex-col gap-1 text-sm font-medium text-gray-700">
              出版日時 終了
              <input
                className="h-10 rounded-md border border-gray-300 px-3 text-gray-900 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100"
                name="publishedAtEnd"
                type="datetime-local"
                defaultValue={toDateTimeLocalValue(query.publishedAtEnd)}
              />
            </label>
          </div>

          <div className="mt-5 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <label className="flex items-center gap-2 text-sm font-medium text-gray-700">
              表示件数
              <select
                className="h-10 rounded-md border border-gray-300 px-3 text-gray-900 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100"
                name="limit"
                defaultValue={query.limit}
              >
                {limitOptions.map((limit) => (
                  <option key={limit} value={limit}>
                    {limit}件
                  </option>
                ))}
              </select>
            </label>
            <div className="flex gap-2">
              <button
                className="h-10 rounded-md border border-gray-300 px-4 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50"
                type="button"
                onClick={() =>
                  void navigate({ search: { searched: false, order: "-published_at", offset: 0, limit: 20 } })
                }
              >
                クリア
              </button>
              <button
                className="h-10 rounded-md bg-gray-900 px-4 text-sm font-medium text-white transition-colors hover:bg-gray-700"
                type="submit"
              >
                検索
              </button>
            </div>
          </div>
        </form>

        <section className="rounded-lg bg-white shadow">
          <div className="flex flex-col gap-3 border-b border-gray-200 p-4 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h3 className="text-lg font-semibold text-gray-900">検索結果</h3>
              <p className="mt-1 text-sm text-gray-600">
                {!query.searched
                  ? "検索条件を入力して検索してください。"
                  : isLoading
                    ? "検索中です。"
                    : `${totalCount.toLocaleString()}件中 ${returnedCount.toLocaleString()}件を表示`}
              </p>
            </div>
            <div className="flex items-center gap-2 text-sm text-gray-600">
              <span>
                {totalPages > 0 ? `${currentPage.toLocaleString()} / ${totalPages.toLocaleString()}ページ` : "0ページ"}
              </span>
              <button
                className="h-9 rounded-md border border-gray-300 px-3 font-medium text-gray-700 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-40"
                type="button"
                disabled={!query.searched || !canGoPrevious || isLoading}
                onClick={() =>
                  void navigate({
                    search: { ...query, searched: true, offset: Math.max(0, query.offset - query.limit) },
                  })
                }
              >
                前へ
              </button>
              <button
                className="h-9 rounded-md border border-gray-300 px-3 font-medium text-gray-700 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-40"
                type="button"
                disabled={!query.searched || !canGoNext || isLoading}
                onClick={() =>
                  void navigate({ search: { ...query, searched: true, offset: query.offset + query.limit } })
                }
              >
                次へ
              </button>
            </div>
          </div>

          {!query.searched ? (
            <div className="p-6 text-sm text-gray-500">検索を実行すると結果一覧が表示されます。</div>
          ) : errorMessage ? (
            <div className="p-6 text-sm text-red-700">{errorMessage}</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200 text-sm">
                <thead className="bg-gray-50 text-left text-xs font-semibold uppercase tracking-wide text-gray-600">
                  <tr>
                    <th className="px-4 py-3">ID</th>
                    <th className="px-4 py-3">ISBN</th>
                    <th className="px-4 py-3">タイトル</th>
                    <th className="px-4 py-3">著者</th>
                    <th className="px-4 py-3">出版社</th>
                    <th className="px-4 py-3 text-right">価格</th>
                    <th className="px-4 py-3">ステータス</th>
                    <th className="px-4 py-3">出版日時</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 bg-white">
                  {isLoading ? (
                    <tr>
                      <td className="px-4 py-8 text-center text-gray-500" colSpan={8}>
                        読み込み中です。
                      </td>
                    </tr>
                  ) : items.length === 0 ? (
                    <tr>
                      <td className="px-4 py-8 text-center text-gray-500" colSpan={8}>
                        条件に一致する書籍はありません。
                      </td>
                    </tr>
                  ) : (
                    items.map((book) => (
                      <tr key={book.id} className="hover:bg-gray-50">
                        <td className="whitespace-nowrap px-4 py-3 font-medium text-gray-900">{book.id}</td>
                        <td className="whitespace-nowrap px-4 py-3 text-gray-700">{book.isbn}</td>
                        <td className="min-w-64 px-4 py-3 text-gray-900">{book.title}</td>
                        <td className="whitespace-nowrap px-4 py-3 text-gray-700">{book.authorName ?? "-"}</td>
                        <td className="whitespace-nowrap px-4 py-3 text-gray-700">{book.publisherName ?? "-"}</td>
                        <td className="whitespace-nowrap px-4 py-3 text-right text-gray-700">
                          {formatPrice(book.price)}
                        </td>
                        <td className="whitespace-nowrap px-4 py-3 text-gray-700">{bookStatusLabels[book.status]}</td>
                        <td className="whitespace-nowrap px-4 py-3 text-gray-700">
                          {formatDateTime(book.publishedAt)}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </main>

      <footer className="bg-white border-t h-16 flex items-center px-6">
        <p className="text-sm text-gray-600">Web Tool</p>
      </footer>
    </div>
  );
}

function toApiQuery(query: SearchParams): SearchBooksQuery {
  return removeUndefined({
    isbn: query.isbn,
    title: query.title,
    priceFrom: query.priceFrom,
    priceTo: query.priceTo,
    status: query.status ? [query.status] : undefined,
    publishedAtStart: query.publishedAtStart,
    publishedAtEnd: query.publishedAtEnd,
    order: query.order,
    offset: query.offset,
    limit: query.limit,
  });
}

function toDownloadQuery(query: SearchParams) {
  const { searched: _searched, offset: _offset, limit: _limit, ...downloadQuery } = query;
  return downloadQuery;
}

function getSearchFromForm(formData: FormData, currentQuery: SearchParams): SearchParams {
  const limit = getNumberFormValue(formData, "limit") ?? currentQuery.limit;

  return removeUndefined({
    searched: true,
    isbn: getStringFormValue(formData, "isbn"),
    title: getStringFormValue(formData, "title"),
    status: getStatusFormValue(formData),
    priceFrom: getNumberFormValue(formData, "priceFrom"),
    priceTo: getNumberFormValue(formData, "priceTo"),
    publishedAtStart: toApiDateTime(getStringFormValue(formData, "publishedAtStart")),
    publishedAtEnd: toApiDateTime(getStringFormValue(formData, "publishedAtEnd")),
    order: getOrderFormValue(formData) ?? currentQuery.order,
    offset: 0,
    limit,
  });
}

function getSearchFormKey(query: SearchParams) {
  return [
    query.isbn,
    query.title,
    query.status,
    query.priceFrom,
    query.priceTo,
    query.publishedAtStart,
    query.publishedAtEnd,
    query.order,
    query.limit,
    query.searched,
  ].join("|");
}

function getStringFormValue(formData: FormData, key: string) {
  const value = formData.get(key);
  return typeof value === "string" && value.trim() !== "" ? value.trim() : undefined;
}

function getNumberFormValue(formData: FormData, key: string) {
  const value = getStringFormValue(formData, key);
  return value === undefined ? undefined : Number(value);
}

function getStatusFormValue(formData: FormData): SearchParams["status"] {
  const value = getStringFormValue(formData, "status");
  return bookStatuses.find((status) => status === value);
}

function getOrderFormValue(formData: FormData): SearchParams["order"] | undefined {
  const value = getStringFormValue(formData, "order");
  return orderOptions.find((order) => order.value === value)?.value;
}

function removeUndefined<T extends Record<string, unknown>>(value: T) {
  return Object.fromEntries(Object.entries(value).filter(([, entryValue]) => entryValue !== undefined)) as T;
}

function toApiDateTime(value: string | undefined) {
  if (!value) {
    return undefined;
  }

  return new Date(value).toISOString();
}

function toDateTimeLocalValue(value: string | undefined) {
  if (!value) {
    return "";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "";
  }

  const offsetMilliseconds = date.getTimezoneOffset() * 60_000;
  return new Date(date.getTime() - offsetMilliseconds).toISOString().slice(0, 16);
}

function formatPrice(price: number | undefined) {
  return price === undefined ? "-" : `${price.toLocaleString()}円`;
}

function formatDateTime(value: string | undefined) {
  if (!value) {
    return "-";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("ja-JP", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

function formatError(error: ErrorResponse | unknown) {
  if (isErrorResponse(error)) {
    return error.message ?? error.code ?? "書籍検索に失敗しました。";
  }

  return "書籍検索に失敗しました。";
}

function isErrorResponse(error: unknown): error is ErrorResponse {
  return typeof error === "object" && error !== null && ("message" in error || "code" in error);
}
