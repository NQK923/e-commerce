"use client";

import { useEffect, useState } from "react";
import { flashSaleApi, FlashSale } from "@/src/api/flashSaleApi";
import { orderApi } from "@/src/api/orderApi";
import { useAuth } from "@/src/store/auth-store";
import { useRouter } from "next/navigation";
import { Card } from "@/src/components/ui/card";
import { Badge } from "@/src/components/ui/badge";
import { Skeleton } from "@/src/components/ui/skeleton";
import { formatCurrency } from "@/src/utils/format";
import { Button } from "@/src/components/ui/button";
import { Clock, Zap } from "lucide-react";

export default function FlashSalesPage() {
  const [sales, setSales] = useState<FlashSale[]>([]);
  const [loading, setLoading] = useState(true);
  const { user } = useAuth();
  const router = useRouter();

  useEffect(() => {
    const fetchSales = async () => {
      setLoading(true);
      try {
        const data = await flashSaleApi.listActive();
        setSales(data);
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };
    fetchSales();
  }, []);

  const handleBuy = async (sale: FlashSale) => {
    if (!user) {
        // Redirect to login with return url
        const returnUrl = encodeURIComponent("/flash-sales");
        router.push(`/login?next=${returnUrl}`);
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
        alert("Đặt hàng thành công!"); // Could be improved with Toast
        router.push("/orders");
    } catch (e) {
        alert("Lỗi khi mua hàng: " + e);
    }
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex items-center gap-3 mb-8">
          <div className="p-3 bg-red-100 rounded-full">
              <Zap className="h-8 w-8 text-red-600 fill-red-600 animate-pulse" />
          </div>
          <div>
              <h1 className="text-3xl font-bold text-zinc-900">Flash Sales</h1>
              <p className="text-zinc-500">Săn deal hot, số lượng có hạn!</p>
          </div>
      </div>

      {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              {[...Array(4)].map((_, i) => (
                  <div key={i} className="flex flex-col space-y-3">
                      <Skeleton className="h-[250px] w-full rounded-xl" />
                      <div className="space-y-2">
                          <Skeleton className="h-4 w-[250px]" />
                          <Skeleton className="h-4 w-[200px]" />
                      </div>
                  </div>
              ))}
          </div>
      ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {sales.length === 0 ? (
                <div className="col-span-full py-12 text-center text-zinc-500 bg-white rounded-xl border border-dashed">
                    Không có chương trình Flash Sale nào đang diễn ra.
                </div>
            ) : (
                sales.map(sale => {
                    const progress = ((sale.totalQuantity - sale.remainingQuantity) / sale.totalQuantity) * 100;
                    return (
                      <Card key={sale.id.value} className="overflow-hidden border-zinc-200 hover:shadow-lg transition-shadow bg-white flex flex-col">
                        <div className="relative aspect-square bg-zinc-100 flex items-center justify-center">
                            {/* Placeholder image logic since FlashSale doesn't have image yet. 
                                In real app, we would fetch product details to get image. */}
                            <div className="text-zinc-400 font-medium">Không có ảnh</div>
                            <div className="absolute top-3 left-3">
                                <Badge className="bg-red-600 text-white border-red-600 animate-pulse">
                                    Đang giảm giá
                                </Badge>
                            </div>
                        </div>

                        <div className="p-4 flex-1 flex flex-col">
                            <h3 className="font-semibold text-zinc-900 mb-2 truncate" title={sale.productId}>
                                {sale.productId} {/* Should ideally be Product Name */}
                            </h3>
                            
                            <div className="flex items-baseline gap-2 mb-3">
                                <span className="text-xl font-bold text-red-600">
                                    {formatCurrency(sale.price.amount, sale.price.currency)}
                                </span>
                                <span className="text-sm text-zinc-400 line-through">
                                    {formatCurrency(sale.originalPrice.amount, sale.originalPrice.currency)}
                                </span>
                            </div>

                            <div className="space-y-2 mt-auto">
                                <div className="flex justify-between text-xs text-zinc-500 mb-1">
                                    <span>Đã bán: {sale.totalQuantity - sale.remainingQuantity}</span>
                                    <span>Còn lại: {sale.remainingQuantity}</span>
                                </div>
                                <div className="h-2 w-full bg-zinc-100 rounded-full overflow-hidden">
                                    <div 
                                        className="h-full bg-red-500 transition-all duration-500" 
                                        style={{ width: `${progress}%` }} 
                                    />
                                </div>
                            </div>
                            
                            <div className="mt-4 pt-4 border-t border-zinc-100 flex items-center justify-between text-xs text-zinc-500">
                                <div className="flex items-center gap-1">
                                    <Clock size={14} />
                                    <span>Kết thúc: {new Date(sale.endTime).toLocaleDateString()}</span>
                                </div>
                            </div>

                            <Button 
                                onClick={() => handleBuy(sale)}
                                className="mt-4 w-full bg-red-600 hover:bg-red-700 text-white font-semibold"
                                disabled={sale.remainingQuantity <= 0}
                            >
                                {sale.remainingQuantity > 0 ? "Mua Ngay" : "Hết Hàng"}
                            </Button>
                        </div>
                      </Card>
                    );
                })
            )}
          </div>
      )}
    </div>
  );
}