"use client";

import { useEffect, useState } from "react";
import { flashSaleApi, FlashSale } from "@/api/flashSaleApi";
import { orderApi } from "@/api/orderApi";
import { useAuth } from "@/store/auth-store";
import { useRouter } from "next/navigation";

export default function FlashSalesPage() {
  const [sales, setSales] = useState<FlashSale[]>([]);
  const { user } = useAuth();
  const router = useRouter();

  useEffect(() => {
    flashSaleApi.listActive().then(setSales).catch(console.error);
  }, []);

  const handleBuy = async (sale: FlashSale) => {
    if (!user) {
        alert("Please login");
        return;
    }
    
    try {
        await orderApi.create({
            userId: user.id, 
            currency: sale.price.currency,
            items: [{
                productId: sale.productId,
                flashSaleId: sale.id.value,
                quantity: 1,
                price: sale.price.amount
            }],
            address: {
                fullName: "Test User",
                line1: "123 Street",
                city: "City",
                state: "State",
                postalCode: "12345",
                country: "Country"
            },
            paymentMethod: "CREDIT_CARD"
        });
        alert("Order placed successfully!");
        router.push("/orders");
    } catch (e) {
        alert("Failed to purchase: " + e);
    }
  };

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">Active Flash Sales</h1>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {sales.map(sale => (
          <div key={sale.id.value} className="border p-4 rounded shadow">
            <h2 className="text-xl font-semibold">Product: {sale.productId}</h2>
            <div className="text-red-600 font-bold text-lg">
                {sale.price.amount} {sale.price.currency}
            </div>
            <div className="text-gray-500 line-through text-sm">
                {sale.originalPrice.amount} {sale.originalPrice.currency}
            </div>
            <div className="mt-2">
                Remaining: {sale.remainingQuantity} / {sale.totalQuantity}
            </div>
            <div className="mt-2 text-sm text-gray-600">
                Ends: {new Date(sale.endTime).toLocaleString()}
            </div>
            <button 
                onClick={() => handleBuy(sale)}
                className="mt-4 bg-red-500 text-white px-4 py-2 rounded w-full hover:bg-red-600 disabled:opacity-50"
                disabled={sale.remainingQuantity <= 0}
            >
                {sale.remainingQuantity > 0 ? "Buy Now" : "Sold Out"}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
