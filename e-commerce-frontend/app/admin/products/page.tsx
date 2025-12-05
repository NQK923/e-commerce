'use client';

import React, { Suspense } from "react";
import { productApi } from "@/src/api/productApi";
import { Badge } from "@/src/components/ui/badge";
import { Button } from "@/src/components/ui/button";
import { Spinner } from "@/src/components/ui/spinner";
import { Product } from "@/src/types/product";
import { MoreHorizontal, Package2 } from "lucide-react";

function ProductsContent() {
  const [products, setProducts] = React.useState<Product[]>([]);
  const [loading, setLoading] = React.useState(true);

  React.useEffect(() => {
    const loadProducts = async () => {
      try {
        const response = await productApi.list({ page: 0, size: 20 });
        setProducts(response.items || []);
      } catch (error) {
        console.error("Failed to load products", error);
      } finally {
        setLoading(false);
      }
    };
    void loadProducts();
  }, []);

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Spinner />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-zinc-900">Products</h1>
          <p className="text-sm text-zinc-500">Manage your product catalog.</p>
        </div>
        <Button>Add Product</Button>
      </div>

      <div className="rounded-xl border border-zinc-200 bg-white shadow-sm overflow-hidden">
        <table className="w-full text-left text-sm">
          <thead className="bg-zinc-50 text-zinc-500 font-medium">
            <tr>
              <th className="px-6 py-4">Product</th>
              <th className="px-6 py-4">Category</th>
              <th className="px-6 py-4">Price</th>
              <th className="px-6 py-4">Inventory</th>
              <th className="px-6 py-4">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-zinc-100">
            {products.map((product) => (
              <tr key={product.id} className="hover:bg-zinc-50/50">
                <td className="px-6 py-4">
                  <div className="flex items-center gap-3">
                    <div className="h-10 w-10 rounded-lg bg-zinc-100 flex items-center justify-center text-zinc-400">
                        <Package2 size={20} />
                    </div>
                    <div>
                      <div className="font-semibold text-zinc-900 line-clamp-1">{product.name}</div>
                      <div className="text-xs text-zinc-500 line-clamp-1">{product.description}</div>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4">
                    <Badge variant="outline">{product.category || "Uncategorized"}</Badge>
                </td>
                <td className="px-6 py-4 font-medium text-zinc-900">
                   {product.currency} {product.price}
                </td>
                <td className="px-6 py-4 text-zinc-500">
                   -
                </td>
                <td className="px-6 py-4">
                  <Button variant="ghost" size="icon">
                    <MoreHorizontal size={16} />
                  </Button>
                </td>
              </tr>
            ))}
            {products.length === 0 && (
                <tr>
                    <td colSpan={5} className="text-center py-8 text-zinc-500">No products found.</td>
                </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default function ProductsPage() {
  return (
    <Suspense fallback={<Spinner />}>
      <ProductsContent />
    </Suspense>
  );
}
