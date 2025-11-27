'use client';

import React, { useMemo, useState } from "react";
import { ProductCard } from "@/src/components/product/product-card";
import { Button } from "@/src/components/ui/button";
import { Input } from "@/src/components/ui/input";
import { Spinner } from "@/src/components/ui/spinner";
import { useProducts } from "@/src/hooks/use-products";
import { useCart } from "@/src/store/cart-store";
import { useToast } from "@/src/components/ui/toast-provider";
import { Product } from "@/src/types/product";

export default function ProductsPage() {
  const { addItem } = useCart();
  const { addToast } = useToast();
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [minPrice, setMinPrice] = useState<string>("");
  const [maxPrice, setMaxPrice] = useState<string>("");

  const params = useMemo(
    () => ({
      page,
      size: 12,
      search: search || undefined,
      minPrice: minPrice ? Number(minPrice) : undefined,
      maxPrice: maxPrice ? Number(maxPrice) : undefined,
    }),
    [maxPrice, minPrice, page, search],
  );

  const { data, loading, error, reload } = useProducts(params);

  const handleAdd = async (product: Product) => {
    await addItem(product, 1);
    addToast(`${product.name} added to cart`, "success");
  };

  const applyFilters = (event: React.FormEvent) => {
    event.preventDefault();
    setPage(0);
  };

  const handleResetFilters = async () => {
    setSearch("");
    setMinPrice("");
    setMaxPrice("");
    setPage(0);
  };

  const totalPages = data ? Math.max(1, Math.ceil(data.total / (data.size || 12))) : 1;

  return (
    <div className="mx-auto flex max-w-6xl flex-col gap-6 px-4 py-10">
      <div className="flex flex-col gap-2">
        <h1 className="text-3xl font-bold text-black">Products</h1>
        <p className="text-sm text-zinc-600">Search, filter, and add products to your cart.</p>
      </div>

      <form
        className="grid gap-3 rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm md:grid-cols-[2fr,1fr,1fr,auto]"
        onSubmit={applyFilters}
      >
        <Input
          label="Search"
          placeholder="Search products"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <Input
          label="Min price"
          type="number"
          value={minPrice}
          onChange={(e) => setMinPrice(e.target.value)}
          placeholder="0"
        />
        <Input
          label="Max price"
          type="number"
          value={maxPrice}
          onChange={(e) => setMaxPrice(e.target.value)}
          placeholder="1000"
        />
        <div className="flex items-end gap-2">
          <Button type="submit" className="w-full">
            Apply
          </Button>
          <Button type="button" variant="secondary" onClick={handleResetFilters}>
            Reset
          </Button>
        </div>
      </form>

      {loading && (
        <div className="flex items-center gap-3 text-sm text-zinc-600">
          <Spinner />
          Loading products...
        </div>
      )}

      {error && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
          {error}
          <Button variant="secondary" className="ml-3" onClick={reload}>
            Retry
          </Button>
        </div>
      )}

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {data?.items.map((product) => (
          <ProductCard key={product.id} product={product} onAddToCart={handleAdd} />
        ))}
      </div>

      {data && !data.items.length && (
        <div className="rounded-xl border border-dashed border-zinc-200 bg-white p-6 text-sm text-zinc-600">
          No products found for your filters.
        </div>
      )}

      {data && (
        <div className="flex items-center justify-between rounded-xl border border-zinc-200 bg-white p-4 text-sm">
          <span>
            Page {page + 1} of {totalPages}
          </span>
          <div className="flex gap-2">
            <Button variant="secondary" disabled={page === 0} onClick={() => setPage((p) => Math.max(0, p - 1))}>
              Previous
            </Button>
            <Button
              variant="secondary"
              disabled={page + 1 >= totalPages}
              onClick={() => setPage((p) => p + 1)}
            >
              Next
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
