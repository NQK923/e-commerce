'use client';

import Link from "next/link";
import React from "react";
import { 
  Truck, 
  ShieldCheck, 
  Clock, 
  ArrowRight, 
  Smartphone, 
  Shirt, 
  Watch, 
  Home as HomeIcon, 
  ChevronRight
} from "lucide-react";
import { productApi } from "@/src/api/productApi";
import { ProductCard } from "@/src/components/product/product-card";
import { Button } from "@/src/components/ui/button";
import { Spinner } from "@/src/components/ui/spinner";
import { useCart } from "@/src/store/cart-store";
import { useToast } from "@/src/components/ui/toast-provider";
import { Product } from "@/src/types/product";

export default function HomePage() {
  const { addItem } = useCart();
  const { addToast } = useToast();
  const [products, setProducts] = React.useState<Product[]>([]);
  const [loading, setLoading] = React.useState(true);

  React.useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const response = await productApi.list({ size: 8, page: 0 });
        setProducts(response.items);
      } catch (error) {
        const message = error instanceof Error ? error.message : "Failed to load products";
        addToast(message, "error");
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, [addToast]);

  const handleAdd = async (product: Product) => {
    await addItem(product, 1);
    addToast(`${product.name} added to cart`, "success");
  };

  return (
    <div className="flex flex-col gap-16 pb-20">
      {/* Hero Section */}
      <section className="relative overflow-hidden bg-black py-24 text-center text-white sm:py-32">
        <div className="absolute inset-0 bg-[linear-gradient(to_right,#4f4f4f2e_1px,transparent_1px),linear-gradient(to_bottom,#4f4f4f2e_1px,transparent_1px)] bg-[size:4rem_4rem] [mask-image:radial-gradient(ellipse_60%_50%_at_50%_0%,#000_70%,transparent_100%)]" />
        <div className="relative mx-auto flex max-w-4xl flex-col items-center gap-6 px-4">
          <div className="inline-flex items-center rounded-full border border-white/20 bg-white/10 px-3 py-1 text-sm font-medium backdrop-blur">
            <span className="mr-2 inline-block h-2 w-2 animate-pulse rounded-full bg-emerald-500" />
            New Collection Available
          </div>
          <h1 className="text-5xl font-extrabold tracking-tight sm:text-7xl">
            Elevate Your <span className="text-zinc-400">Lifestyle</span>
          </h1>
          <p className="max-w-2xl text-lg text-zinc-400 sm:text-xl">
            Discover a curated selection of premium products designed for modern living. 
            Quality meets aesthetics in every piece we offer.
          </p>
          <div className="mt-4 flex flex-wrap justify-center gap-4">
            <Link href="/products">
              <Button size="lg" className="h-12 px-8 text-base bg-white text-black hover:bg-zinc-200 border-0">
                Shop Now <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
            </Link>
            <Link href="/register">
              <Button size="lg" variant="outline" className="h-12 px-8 text-base text-white border-zinc-700 hover:bg-zinc-800 hover:text-white">
                Create Account
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Features / Trust Signals */}
      <section className="mx-auto grid max-w-7xl grid-cols-1 gap-8 px-4 sm:grid-cols-3">
        {[
          { icon: Truck, title: "Free Shipping", desc: "On all orders over $50" },
          { icon: ShieldCheck, title: "Secure Payment", desc: "100% secure transaction" },
          { icon: Clock, title: "24/7 Support", desc: "Dedicated support team" },
        ].map((feature, i) => (
          <div key={i} className="flex items-center gap-4 rounded-2xl border border-zinc-100 bg-white p-6 shadow-sm transition-shadow hover:shadow-md">
            <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-zinc-100 text-black">
              <feature.icon size={24} />
            </div>
            <div>
              <h3 className="font-bold text-zinc-900">{feature.title}</h3>
              <p className="text-sm text-zinc-500">{feature.desc}</p>
            </div>
          </div>
        ))}
      </section>

      {/* Categories */}
      <section className="mx-auto w-full max-w-7xl px-4">
        <div className="mb-8 flex items-end justify-between">
          <h2 className="text-3xl font-bold tracking-tight text-zinc-900">Shop by Category</h2>
          <Link href="/products" className="hidden text-sm font-medium text-zinc-500 hover:text-black sm:block">
            View all categories &rarr;
          </Link>
        </div>
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
          {[
            { name: "Electronics", icon: Smartphone, color: "bg-blue-50 text-blue-600" },
            { name: "Fashion", icon: Shirt, color: "bg-rose-50 text-rose-600" },
            { name: "Accessories", icon: Watch, color: "bg-amber-50 text-amber-600" },
            { name: "Home", icon: HomeIcon, color: "bg-emerald-50 text-emerald-600" },
          ].map((cat) => (
            <Link 
              key={cat.name} 
              href={`/products?category=${cat.name.toLowerCase()}`}
              className="group flex flex-col items-center justify-center rounded-xl border border-zinc-100 bg-white py-10 transition-all hover:border-zinc-300 hover:shadow-sm"
            >
              <div className={`mb-4 flex h-16 w-16 items-center justify-center rounded-full ${cat.color} transition-transform group-hover:scale-110`}>
                <cat.icon size={32} />
              </div>
              <span className="font-semibold text-zinc-900">{cat.name}</span>
            </Link>
          ))}
        </div>
      </section>

      {/* Featured Products */}
      <section className="mx-auto w-full max-w-7xl px-4">
        <div className="mb-8 flex items-end justify-between">
          <div>
            <h2 className="text-3xl font-bold tracking-tight text-zinc-900">Featured Products</h2>
            <p className="mt-2 text-zinc-500">Hand-picked daily essentials just for you.</p>
          </div>
          <Link href="/products">
            <Button variant="ghost" className="gap-1">
              View all <ChevronRight size={16} />
            </Button>
          </Link>
        </div>

        {loading ? (
          <div className="flex h-64 flex-col items-center justify-center gap-4 rounded-xl border border-dashed border-zinc-200 bg-zinc-50 text-zinc-500">
            <Spinner />
            <p>Loading best sellers...</p>
          </div>
        ) : (
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {products.map((product) => (
              <ProductCard key={product.id} product={product} onAddToCart={handleAdd} />
            ))}
            {!products.length && (
              <div className="col-span-full flex h-40 items-center justify-center rounded-xl border border-dashed border-zinc-200 bg-zinc-50 text-zinc-500">
                No products found.
              </div>
            )}
          </div>
        )}
      </section>

      {/* Newsletter CTA */}
      <section className="mx-4 rounded-3xl bg-zinc-900 px-6 py-16 text-center text-white sm:mx-auto sm:max-w-7xl sm:px-16">
        <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">Stay in the loop</h2>
        <p className="mx-auto mt-4 max-w-xl text-lg text-zinc-400">
          Subscribe to our newsletter for exclusive offers, new arrivals, and style tips directly to your inbox.
        </p>
        <form className="mx-auto mt-8 flex max-w-md flex-col gap-3 sm:flex-row" onSubmit={(e) => e.preventDefault()}>
          <input 
            type="email" 
            placeholder="Enter your email" 
            className="flex-1 rounded-lg border-0 bg-white/10 px-4 py-3 text-white placeholder:text-zinc-500 focus:ring-2 focus:ring-white/50"
            required
          />
          <Button size="lg" className="bg-white text-black hover:bg-zinc-200">
            Subscribe
          </Button>
        </form>
      </section>
    </div>
  );
}
