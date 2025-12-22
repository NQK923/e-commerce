'use client';

import React, { Suspense, useEffect, useMemo, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { Filter, SlidersHorizontal, X } from "lucide-react";
import { ProductCard } from "@/src/components/product/product-card";
import { Button } from "@/src/components/ui/button";
import { Input } from "@/src/components/ui/input";
import { Spinner } from "@/src/components/ui/spinner";
import { useProducts } from "@/src/hooks/use-products";
import { useTranslation } from "@/src/providers/language-provider";
import { useDebounce } from "@/src/hooks/use-debounce";
import { PRODUCT_CATEGORIES } from "@/src/constants/categories";

const SORT_OPTIONS = [
  { label: "newest_arrivals", value: "createdAt,desc" },
  { label: "price_low_high", value: "price,asc" },
  { label: "price_high_low", value: "price,desc" },
  { label: "best_selling", value: "soldCount,desc" },
];

function ProductsContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { t } = useTranslation();

  // Initialize state from URL
  const initialSearch = searchParams.get("search") || "";
  const initialCategory = searchParams.get("category") || "";
  const initialMinPrice = searchParams.get("minPrice") || "";
  const initialMaxPrice = searchParams.get("maxPrice") || "";
  const initialSort = searchParams.get("sort") || SORT_OPTIONS[0].value;
  const initialPage = Number(searchParams.get("page")) || 0;

  const [search, setSearch] = useState(initialSearch);
  const [category, setCategory] = useState(initialCategory);
  const [minPrice, setMinPrice] = useState(initialMinPrice);
  const [maxPrice, setMaxPrice] = useState(initialMaxPrice);
  const [sort, setSort] = useState(initialSort);
  const [page, setPage] = useState(initialPage);
  const [showMobileFilters, setShowMobileFilters] = useState(false);

  const debouncedSearch = useDebounce(search, 500);
  const debouncedMinPrice = useDebounce(minPrice, 500);
  const debouncedMaxPrice = useDebounce(maxPrice, 500);

  // Sync state to URL
  useEffect(() => {
    const params = new URLSearchParams();
    if (debouncedSearch) params.set("search", debouncedSearch);
    if (category) params.set("category", category);
    if (debouncedMinPrice) params.set("minPrice", debouncedMinPrice);
    if (debouncedMaxPrice) params.set("maxPrice", debouncedMaxPrice);
    if (sort) params.set("sort", sort);
    if (page > 0) params.set("page", page.toString());

    router.replace(`/products?${params.toString()}`, { scroll: false });
  }, [debouncedSearch, category, debouncedMinPrice, debouncedMaxPrice, sort, page, router]);

  const params = useMemo(
    () => ({
      page,
      size: 12,
      search: debouncedSearch || undefined,
      category: category || undefined,
      minPrice: debouncedMinPrice ? Number(debouncedMinPrice) : undefined,
      maxPrice: debouncedMaxPrice ? Number(debouncedMaxPrice) : undefined,
      sort,
    }),
    [category, debouncedMaxPrice, debouncedMinPrice, page, debouncedSearch, sort],
  );

  const { data, loading, error, reload } = useProducts(params);

  const clearFilters = () => {
    setSearch("");
    setCategory("");
    setMinPrice("");
    setMaxPrice("");
    setSort(SORT_OPTIONS[0].value);
    setPage(0);
  };

  const totalPages = data ? Math.max(1, Math.ceil(data.total / (data.size || 12))) : 1;

  return (
    <div className="mx-auto max-w-7xl px-4 py-8">
      {/* Mobile Filter Toggle */}
      <div className="mb-6 flex items-center justify-between lg:hidden">
        <h1 className="text-2xl font-bold text-zinc-900">{t.nav.products}</h1>
        <Button variant="outline" size="sm" onClick={() => setShowMobileFilters(!showMobileFilters)}>
          <Filter size={16} className="mr-2" /> {t.home.categories.title}
        </Button>
      </div>

      <div className="grid gap-8 lg:grid-cols-[280px_1fr]">
        {/* Sidebar Filters */}
        <aside 
          className={`
            fixed inset-0 z-40 bg-white p-6 transition-transform duration-300 lg:static lg:block lg:translate-x-0 lg:p-0
            ${showMobileFilters ? "translate-x-0" : "-translate-x-full"}
          `}
        >
          <div className="flex items-center justify-between mb-6 lg:hidden">
             <h3 className="font-bold text-lg">{t.home.categories.title}</h3>
             <Button variant="ghost" size="icon" onClick={() => setShowMobileFilters(false)}>
               <X size={20} />
             </Button>
          </div>

          <div className="sticky top-24 space-y-8">
            <div>
              <div className="flex items-center justify-between">
                <h3 className="font-bold text-zinc-900">{t.home.categories.title}</h3>
                <button onClick={clearFilters} className="text-xs text-emerald-600 hover:underline">
                  {t.nav.clear_filters}
                </button>
              </div>
              <div className="mt-4 space-y-4">
                {/* Search */}
                <div>
                  <label className="text-xs font-semibold text-zinc-500 uppercase">{t.nav.search_placeholder}</label>
                  <Input 
                    placeholder={t.nav.search_placeholder} 
                    value={search} 
                    onChange={(e) => { setSearch(e.target.value); setPage(0); }} 
                    className="mt-1.5"
                  />
                </div>

                {/* Category */}
                <div>
                  <label className="text-xs font-semibold text-zinc-500 uppercase">{t.home.categories.title}</label>
                  <div className="mt-2 space-y-2 max-h-[300px] overflow-y-auto">
                    <label className="flex items-center gap-2 text-sm text-zinc-700 cursor-pointer hover:text-emerald-600 transition-colors">
                      <input 
                        type="radio" 
                        name="category" 
                        checked={category === ""} 
                        onChange={() => { setCategory(""); setPage(0); }}
                        className="accent-emerald-600 h-4 w-4"
                      />
                      {t.home.categories.view_all}
                    </label>
                    {PRODUCT_CATEGORIES.map((cat) => (
                      <label key={cat.value} className="flex items-center gap-2 text-sm text-zinc-700 cursor-pointer hover:text-emerald-600 transition-colors">
                        <input 
                          type="radio" 
                          name="category" 
                          value={cat.value} 
                          checked={category === cat.value} 
                          onChange={() => { setCategory(cat.value); setPage(0); }}
                          className="accent-emerald-600 h-4 w-4"
                        />
                        {cat.label}
                      </label>
                    ))}
                  </div>
                </div>

                {/* Price Range */}
                <div>
                  <label className="text-xs font-semibold text-zinc-500 uppercase">{t.nav.price_range} ({t.product.currency})</label>
                  <div className="mt-2 flex items-center gap-2">
                    <Input 
                      type="number" 
                      placeholder={t.nav.min_price} 
                      value={minPrice} 
                      onChange={(e) => { setMinPrice(e.target.value); setPage(0); }}
                      className="h-9 text-sm"
                    />
                    <span className="text-zinc-400">-</span>
                    <Input 
                      type="number" 
                      placeholder={t.nav.max_price} 
                      value={maxPrice} 
                      onChange={(e) => { setMaxPrice(e.target.value); setPage(0); }}
                      className="h-9 text-sm"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </aside>

        {/* Overlay for mobile */}
        {showMobileFilters && (
          <div 
            className="fixed inset-0 z-30 bg-black/50 lg:hidden"
            onClick={() => setShowMobileFilters(false)}
          />
        )}

        {/* Main Content */}
        <main className="min-h-[500px]">
          {/* Header */}
          <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div className="hidden lg:block">
              <h1 className="text-3xl font-bold text-zinc-900">{t.nav.products}</h1>
              <p className="text-sm text-zinc-500 mt-1">
                {t.nav.showing_results
                    .replace("{{count}}", (data?.items.length || 0).toString())
                    .replace("{{total}}", (data?.total || 0).toString())}
              </p>
            </div>
            
            <div className="flex items-center gap-3 ml-auto">
              <span className="text-sm text-zinc-500">{t.nav.sort_by}</span>
              <select 
                value={sort}
                onChange={(e) => { setSort(e.target.value); setPage(0); }}
                className="h-10 rounded-lg border border-zinc-200 bg-white px-3 text-sm outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100 cursor-pointer hover:border-emerald-400 transition-colors"
              >
                {SORT_OPTIONS.map(opt => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label === "newest_arrivals" ? t.nav.sort_options.newest : 
                     opt.label === "price_low_high" ? t.nav.sort_options.price_low_high :
                     opt.label === "price_high_low" ? t.nav.sort_options.price_high_low : 
                     opt.label === "best_selling" ? t.nav.sort_options.best_selling : opt.label}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Product Grid */}
          {loading ? (
            <div className="flex h-64 items-center justify-center flex-col gap-3 text-zinc-500">
              <Spinner />
              <p>{t.common.loading}</p>
            </div>
          ) : error ? (
            <div className="flex flex-col items-center justify-center rounded-xl border border-rose-200 bg-rose-50 p-8 text-center">
              <p className="text-rose-700 mb-4">{error}</p>
              <Button onClick={reload}>{t.common.retry}</Button>
            </div>
          ) : (
            <>
              <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                {data?.items.map((product) => (
                  <ProductCard key={product.id} product={product} />
                ))}
              </div>

              {!data?.items.length && (
                <div className="flex flex-col items-center justify-center py-20 text-center">
                  <div className="h-16 w-16 rounded-full bg-zinc-100 flex items-center justify-center mb-4 text-zinc-400">
                    <SlidersHorizontal size={32} />
                  </div>
                  <h3 className="text-lg font-semibold text-zinc-900">{t.nav.no_products_found}</h3>
                  <p className="text-zinc-500 max-w-md mt-2">
                    {t.nav.try_adjust_filters}
                  </p>
                  <Button variant="secondary" className="mt-6" onClick={clearFilters}>
                    {t.nav.clear_filters}
                  </Button>
                </div>
              )}

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="mt-10 flex items-center justify-center gap-2">
                  <Button 
                    variant="outline" 
                    disabled={page === 0} 
                    onClick={() => { setPage(p => p - 1); window.scrollTo({ top: 0, behavior: 'smooth' }); }}
                  >
                    {t.nav.previous}
                  </Button>
                  <div className="flex items-center gap-1 px-4 text-sm font-medium">
                    {t.nav.page_info
                        .replace("{{current}}", (page + 1).toString())
                        .replace("{{total}}", totalPages.toString())}
                  </div>
                  <Button 
                    variant="outline" 
                    disabled={page + 1 >= totalPages} 
                    onClick={() => { setPage(p => p + 1); window.scrollTo({ top: 0, behavior: 'smooth' }); }}
                  >
                    {t.nav.next}
                  </Button>
                </div>
              )}
            </>
          )}
        </main>
      </div>
    </div>
  );
}

export default function ProductsPage() {
  const { t } = useTranslation();
  return (
    <Suspense fallback={
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
        {t.common.loading}
      </div>
    }>
      <ProductsContent />
    </Suspense>
  );
}
