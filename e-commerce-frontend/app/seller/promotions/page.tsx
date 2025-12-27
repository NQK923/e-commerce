"use client";

import React, { useEffect, useState } from "react";
import { promotionApi, Coupon, DiscountType } from "@/src/api/promotionApi";
import { productApi } from "@/src/api/productApi";
import { Product } from "@/src/types/product";
import { Button } from "@/src/components/ui/button";
import { Input } from "@/src/components/ui/input";
import { useToast } from "@/src/components/ui/toast-provider";
import { useTranslation } from "@/src/providers/language-provider";
import { Badge } from "@/src/components/ui/badge";
import { Spinner } from "@/src/components/ui/spinner";
import { formatCurrency, formatDate } from "@/src/utils/format";

export default function SellerPromotionsPage() {
  const { addToast } = useToast();
  const { t } = useTranslation();
  const [coupons, setCoupons] = useState<Coupon[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  
  // Form State
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({
      code: "",
      discountType: "PERCENTAGE" as DiscountType,
      discountValue: "",
      minOrderAmount: "",
      maxDiscountAmount: "",
      usageLimit: "",
      startDate: "",
      endDate: "",
      selectedProducts: [] as string[]
  });

  useEffect(() => {
    Promise.all([
        promotionApi.sellerListCoupons(),
        productApi.list({ size: 100 }) // Fetch seller's products
    ]).then(([couponsData, productsData]) => {
        setCoupons(couponsData);
        setProducts(productsData.items);
    }).catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
      e.preventDefault();
      setCreating(true);
      try {
          await promotionApi.sellerCreateCoupon({
              code: formData.code.toUpperCase(),
              discountType: formData.discountType,
              discountValue: parseFloat(formData.discountValue),
              minOrderAmount: formData.minOrderAmount ? parseFloat(formData.minOrderAmount) : undefined,
              maxDiscountAmount: formData.maxDiscountAmount ? parseFloat(formData.maxDiscountAmount) : undefined,
              usageLimit: formData.usageLimit ? parseInt(formData.usageLimit) : 0,
              startAt: new Date(formData.startDate).toISOString(),
              endAt: new Date(formData.endDate).toISOString(),
              applicableProductIds: formData.selectedProducts,
              currency: "USD"
          });
          addToast(t.common.success, "success");
          setShowForm(false);
          const updated = await promotionApi.sellerListCoupons();
          setCoupons(updated);
      } catch (err) {
          addToast(t.common.error, "error");
      } finally {
          setCreating(false);
      }
  };

  const toggleProduct = (id: string) => {
      setFormData(prev => ({
          ...prev,
          selectedProducts: prev.selectedProducts.includes(id) 
              ? prev.selectedProducts.filter(p => p !== id)
              : [...prev.selectedProducts, id]
      }));
  };

  if (loading) return <div className="p-8 flex justify-center"><Spinner /></div>;

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold">Promotions & Coupons</h1>
          <Button onClick={() => setShowForm(!showForm)}>
              {showForm ? "Cancel" : "Create Coupon"}
          </Button>
      </div>

      {showForm && (
          <div className="bg-white p-6 rounded-xl border border-zinc-200 mb-8 shadow-sm">
              <h2 className="text-lg font-semibold mb-4">New Coupon</h2>
              <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <Input 
                          label="Code" 
                          required 
                          placeholder="SUMMER2025" 
                          value={formData.code}
                          onChange={e => setFormData({...formData, code: e.target.value})}
                      />
                      <div>
                          <label className="text-sm font-medium text-zinc-700 mb-1 block">Type</label>
                          <select 
                              className="w-full rounded-md border border-zinc-300 p-2 text-sm"
                              value={formData.discountType}
                              onChange={e => setFormData({...formData, discountType: e.target.value as DiscountType})}
                          >
                              <option value="PERCENTAGE">Percentage (%)</option>
                              <option value="FIXED_AMOUNT">Fixed Amount ($)</option>
                          </select>
                      </div>
                      <Input 
                          label="Value" 
                          type="number" 
                          required 
                          placeholder="10" 
                          value={formData.discountValue}
                          onChange={e => setFormData({...formData, discountValue: e.target.value})}
                      />
                      <Input 
                          label="Min Order Amount" 
                          type="number" 
                          placeholder="50" 
                          value={formData.minOrderAmount}
                          onChange={e => setFormData({...formData, minOrderAmount: e.target.value})}
                      />
                      {formData.discountType === "PERCENTAGE" && (
                          <Input 
                              label="Max Discount Cap" 
                              type="number" 
                              placeholder="20" 
                              value={formData.maxDiscountAmount}
                              onChange={e => setFormData({...formData, maxDiscountAmount: e.target.value})}
                          />
                      )}
                      <Input 
                          label="Usage Limit" 
                          type="number" 
                          placeholder="100" 
                          value={formData.usageLimit}
                          onChange={e => setFormData({...formData, usageLimit: e.target.value})}
                      />
                      <Input 
                          label="Start Date" 
                          type="datetime-local" 
                          required 
                          value={formData.startDate}
                          onChange={e => setFormData({...formData, startDate: e.target.value})}
                      />
                      <Input 
                          label="End Date" 
                          type="datetime-local" 
                          required 
                          value={formData.endDate}
                          onChange={e => setFormData({...formData, endDate: e.target.value})}
                      />
                  </div>

                  <div>
                      <label className="text-sm font-medium text-zinc-700 mb-2 block">Applicable Products (Optional - Leave empty for all)</label>
                      <div className="max-h-48 overflow-y-auto border border-zinc-200 rounded-md p-2 grid grid-cols-1 sm:grid-cols-2 gap-2">
                          {products.map(p => (
                              <div 
                                  key={p.id} 
                                  className={`p-2 rounded-md border cursor-pointer text-sm flex items-center gap-2 ${formData.selectedProducts.includes(p.id) ? 'border-emerald-500 bg-emerald-50' : 'border-zinc-100 hover:bg-zinc-50'}`}
                                  onClick={() => toggleProduct(p.id)}
                              >
                                  <div className={`w-4 h-4 rounded-sm border flex items-center justify-center ${formData.selectedProducts.includes(p.id) ? 'bg-emerald-500 border-emerald-500' : 'border-zinc-300'}`}>
                                      {formData.selectedProducts.includes(p.id) && <span className="text-white text-xs">✓</span>}
                                  </div>
                                  <span className="truncate">{p.name}</span>
                              </div>
                          ))}
                      </div>
                  </div>

                  <div className="flex justify-end pt-4">
                      <Button type="submit" disabled={creating}>
                          {creating ? <Spinner className="mr-2" /> : null}
                          Create Coupon
                      </Button>
                  </div>
              </form>
          </div>
      )}

      <div className="grid gap-4">
          {coupons.length === 0 ? (
              <p className="text-center text-zinc-500 py-12 bg-white rounded-xl border border-dashed">No active coupons</p>
          ) : (
              coupons.map(coupon => (
                  <div key={coupon.id} className="bg-white p-4 rounded-xl border border-zinc-200 flex justify-between items-center">
                      <div>
                          <div className="flex items-center gap-2">
                              <span className="font-mono font-bold text-lg text-emerald-600">{coupon.code}</span>
                              <Badge>{coupon.discountType === "PERCENTAGE" ? `${coupon.discountValue}% OFF` : `-$${coupon.discountValue}`}</Badge>
                          </div>
                          <p className="text-sm text-zinc-500 mt-1">
                              {coupon.applicableProductIds.length > 0 ? `Applies to ${coupon.applicableProductIds.length} products` : "Store-wide"} • 
                              Used: {coupon.usedCount} / {coupon.usageLimit > 0 ? coupon.usageLimit : "∞"}
                          </p>
                          <p className="text-xs text-zinc-400 mt-1">
                              Valid: {formatDate(coupon.startAt)} - {formatDate(coupon.endAt)}
                          </p>
                      </div>
                      <div className="text-right">
                          <div className="text-sm font-semibold">Min Order: {coupon.minOrderValue ? formatCurrency(coupon.minOrderValue.amount, coupon.minOrderValue.currency) : "None"}</div>
                      </div>
                  </div>
              ))
          )}
      </div>
    </div>
  );
}
