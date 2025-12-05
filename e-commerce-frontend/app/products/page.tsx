'use client';

import React, { Suspense, useMemo, useState } from "react";
import { Filter, SlidersHorizontal } from "lucide-react";
import { ProductCard } from "@/src/components/product/product-card";
import { Button } from "@/src/components/ui/button";
import { Input } from "@/src/components/ui/input";
import { Spinner } from "@/src/components/ui/spinner";
import { useProducts } from "@/src/hooks/use-products";
import { useCart } from "@/src/store/cart-store";
import { useToast } from "@/src/components/ui/toast-provider";
import { Product } from "@/src/types/product";

const CATEGORIES = ["Electronics", "Fashion", "Home", "Books", "Beauty", "Sports", "Toys", "Automotive"];
const SORT_OPTIONS = [
  { label: "Newest Arrivals", value: "createdAt,desc" },
  { label: "Price: Low to High", value: "price,asc" },
  { label: "Price: High to Low", value: "price,desc" },
];

function ProductsContent() {
  const { addItem } = useCart();
  const { addToast } = useToast();
  
  // Filter States
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState<string>("");
  const [minPrice, setMinPrice] = useState<string>("");
  const [maxPrice, setMaxPrice] = useState<string>("");
  const [sort, setSort] = useState(SORT_OPTIONS[0].value);
  const [page, setPage] = useState(0);
  const [showMobileFilters, setShowMobileFilters] = useState(false);

  const params = useMemo(
    () => ({
      page,
      size: 12,
      search: search || undefined,
      category: category || undefined,
      minPrice: minPrice ? Number(minPrice) : undefined,
      maxPrice: maxPrice ? Number(maxPrice) : undefined,
      sort,
    }),
    [category, maxPrice, minPrice, page, search, sort],
  );

  const { data, loading, error, reload } = useProducts(params);

  const handleAdd = async (product: Product) => {
    await addItem(product, 1);
    addToast(`${product.name} added to cart`, "success");
  };

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
        <h1 className="text-2xl font-bold text-zinc-900">Products</h1>
        <Button variant="outline" size="sm" onClick={() => setShowMobileFilters(!showMobileFilters)}>
          <Filter size={16} className="mr-2" /> Filters
        </Button>
      </div>

      <div className="grid gap-8 lg:grid-cols-[280px_1fr]">
        {/* Sidebar Filters */}
        <aside className={`space-y-8 ${showMobileFilters ? "block" : "hidden"} lg:block`}>
          <div className="sticky top-24 space-y-8">
            <div>
              <div className="flex items-center justify-between">
                <h3 className="font-bold text-zinc-900">Filters</h3>
                <button onClick={clearFilters} className="text-xs text-emerald-600 hover:underline">
                  Clear All
                </button>
              </div>
              <div className="mt-4 space-y-4">
                {/* Search */}
                <div>
                  <label className="text-xs font-semibold text-zinc-500 uppercase">Search</label>
                  <Input 
                    placeholder="Keyword..." 
                    value={search} 
                    onChange={(e) => { setSearch(e.target.value); setPage(0); }} 
                    className="mt-1.5"
                  />
                </div>

                {/* Category */}
                <div>
                  <label className="text-xs font-semibold text-zinc-500 uppercase">Category</label>
                  <div className="mt-2 space-y-2">
                    <label className="flex items-center gap-2 text-sm text-zinc-700 cursor-pointer">
                      <input 
                        type="radio" 
                        name="category" 
                        checked={category === ""} 
                        onChange={() => { setCategory(""); setPage(0); }}
                        className="accent-emerald-600"
                      />
                      All Categories
                    </label>
                    {CATEGORIES.map((cat) => (
                      <label key={cat} className="flex items-center gap-2 text-sm text-zinc-700 cursor-pointer">
                        <input 
                          type="radio" 
                          name="category" 
                          value={cat} 
                          checked={category === cat} 
                          onChange={() => { setCategory(cat); setPage(0); }}
                          className="accent-emerald-600"
                        />
                        {cat}
                      </label>
                    ))}
                  </div>
                </div>

                {/* Price Range */}
                <div>
                  <label className="text-xs font-semibold text-zinc-500 uppercase">Price Range</label>
                  <div className="mt-2 flex items-center gap-2">
                    <Input 
                      type="number" 
                      placeholder="Min" 
                      value={minPrice} 
                      onChange={(e) => { setMinPrice(e.target.value); setPage(0); }}
                      className="h-9 text-sm"
                    />
                    <span className="text-zinc-400">-</span>
                    <Input 
                      type="number" 
                      placeholder="Max" 
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

        {/* Main Content */}
        <main className="min-h-[500px]">
          {/* Header */}
          <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div className="hidden lg:block">
              <h1 className="text-3xl font-bold text-zinc-900">Shop</h1>
              <p className="text-sm text-zinc-500 mt-1">
                Showing {data?.items.length || 0} of {data?.total || 0} results
              </p>
            </div>
            
            <div className="flex items-center gap-3 ml-auto">
              <span className="text-sm text-zinc-500">Sort by:</span>
              <select 
                value={sort}
                onChange={(e) => { setSort(e.target.value); setPage(0); }}
                className="h-10 rounded-lg border border-zinc-200 bg-white px-3 text-sm outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
              >
                {SORT_OPTIONS.map(opt => (
                  <option key={opt.value} value={opt.value}>{opt.label}</option>
                ))}
              </select>
            </div>
          </div>

          {/* Product Grid */}
          {loading ? (
            <div className="flex h-64 items-center justify-center">
              <Spinner />
            </div>
          ) : error ? (
            <div className="flex flex-col items-center justify-center rounded-xl border border-rose-200 bg-rose-50 p-8 text-center">
              <p className="text-rose-700 mb-4">{error}</p>
              <Button onClick={reload}>Try Again</Button>
            </div>
          ) : (
            <>
              <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                {data?.items.map((product) => (
                  <ProductCard key={product.id} product={product} onAddToCart={handleAdd} />
                ))}
              </div>

              {!data?.items.length && (
                <div className="flex flex-col items-center justify-center py-20 text-center">
                  <div className="h-16 w-16 rounded-full bg-zinc-100 flex items-center justify-center mb-4 text-zinc-400">
                    <SlidersHorizontal size={32} />
                  </div>
                  <h3 className="text-lg font-semibold text-zinc-900">No products found</h3>
                  <p className="text-zinc-500 max-w-md mt-2">
                    Try adjusting your filters or search query to find what you&#39;re looking for.
                  </p>
                  <Button variant="secondary" className="mt-6" onClick={clearFilters}>
                    Clear Filters
                  </Button>
                </div>
              )}

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="mt-10 flex items-center justify-center gap-2">
                  <Button 
                    variant="outline" 
                    disabled={page === 0} 
                    onClick={() => setPage(p => p - 1)}
                  >
                    Previous
                  </Button>
                  <div className="flex items-center gap-1 px-4 text-sm font-medium">
                    Page {page + 1} of {totalPages}
                  </div>
                  <Button 
                    variant="outline" 
                    disabled={page + 1 >= totalPages} 
                    onClick={() => setPage(p => p + 1)}
                  >
                    Next
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
  return (
    <Suspense fallback={
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
        Loading shop...
      </div>
    }>
      <ProductsContent />
    </Suspense>
  );
}
