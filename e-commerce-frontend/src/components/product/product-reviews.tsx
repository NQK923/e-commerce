'use client';

import React, { useEffect, useState } from "react";
import { Star } from "lucide-react";
import { productApi, Review } from "@/src/api/productApi";
import { orderApi } from "@/src/api/orderApi"; // Import orderApi
import { useAuth } from "@/src/store/auth-store";
import { Button } from "@/src/components/ui/button";
import { useToast } from "@/src/components/ui/toast-provider";
import { Spinner } from "@/src/components/ui/spinner";
import clsx from "clsx";

interface ProductReviewsProps {
  productId: string;
}

export function ProductReviews({ productId }: ProductReviewsProps) {
  const { user } = useAuth();
  const { addToast } = useToast();
  const [reviews, setReviews] = useState<Review[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [hasPurchased, setHasPurchased] = useState(false); // New state

  // Form state
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");

  const fetchReviews = React.useCallback(async () => {
    try {
      const resp = await productApi.fetchReviews(productId);
      setReviews(resp.items);
    } catch {
      console.error("Failed to load reviews");
    } finally {
      setLoading(false);
    }
  }, [productId]);

  const checkIfUserPurchasedProduct = React.useCallback(async () => {
    if (!user?.id) {
      setHasPurchased(false);
      return;
    }
    try {
      // Assuming orderApi.list can fetch orders for the current user
      // and contains product IDs in its items.
      const userOrdersResponse = await orderApi.list({ page: 0, size: 100, sort: 'createdAt,desc' }); // Fetch some recent orders
      const purchased = userOrdersResponse.items.some(
        (order) => order.status === 'PAID' && order.items.some((item) => item.productId === productId)
      );
      setHasPurchased(purchased);
    } catch (error) {
      console.error("Failed to check purchase status", error);
      setHasPurchased(false);
    }
  }, [user?.id, productId]);

  useEffect(() => {
    fetchReviews();
    checkIfUserPurchasedProduct();
  }, [fetchReviews, checkIfUserPurchasedProduct]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user || !hasPurchased) return; // Ensure user is logged in and has purchased
    setSubmitting(true);
    try {
      await productApi.addReview(productId, {
        userId: user.id,
        userName: user.displayName || user.email || "User",
        rating,
        comment,
      });
      addToast("Đánh giá của bạn đã được gửi!", "success");
      setComment("");
      setRating(5);
      setShowForm(false);
      fetchReviews(); // Reload
    } catch (error) {
      console.error("Failed to submit review", error);
      addToast("Không thể gửi đánh giá. Vui lòng đảm bảo bạn đã mua sản phẩm này và chưa đánh giá.", "error");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6 pt-8 border-t border-zinc-100">
      <div className="flex items-center justify-between">
        <h3 className="text-xl font-bold text-zinc-900">Đánh giá sản phẩm ({reviews.length})</h3>
        {user && !showForm && hasPurchased && ( // Conditional render based on hasPurchased
          <Button variant="outline" onClick={() => setShowForm(true)}>
            Viết đánh giá
          </Button>
        )}
      </div>

      {!user ? (
        <p className="text-zinc-500 py-4">Đăng nhập để xem hoặc viết đánh giá.</p>
      ) : !hasPurchased && !showForm ? (
        <p className="text-zinc-500 py-4">Bạn cần mua sản phẩm này để viết đánh giá.</p>
      ) : null}

      {showForm && (
        <form onSubmit={handleSubmit} className="bg-zinc-50 p-6 rounded-xl border border-zinc-200 space-y-4">
          <div>
            <label className="block text-sm font-medium text-zinc-700 mb-2">Đánh giá của bạn</label>
            <div className="flex gap-1">
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  key={star}
                  type="button"
                  onClick={() => setRating(star)}
                  className="focus:outline-none"
                >
                  <Star
                    className={clsx(
                      "h-6 w-6 transition-colors",
                      star <= rating ? "fill-yellow-400 text-yellow-400" : "text-zinc-300"
                    )}
                  />
                </button>
              ))}
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-zinc-700 mb-2">Nhận xét</label>
            <textarea
              className="w-full min-h-[100px] p-3 rounded-lg border border-zinc-300 focus:ring-2 focus:ring-emerald-500 outline-none"
              placeholder="Chia sẻ cảm nhận của bạn về sản phẩm..."
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              required
            />
          </div>
          <div className="flex gap-3 justify-end">
            <Button type="button" variant="ghost" onClick={() => setShowForm(false)}>
              Hủy
            </Button>
            <Button type="submit" disabled={submitting} className="bg-emerald-600 hover:bg-emerald-700">
              {submitting ? <Spinner className="w-4 h-4 mr-2" /> : null}
              Gửi đánh giá
            </Button>
          </div>
        </form>
      )}

      {loading ? (
        <div className="flex justify-center py-8">
          <Spinner />
        </div>
      ) : reviews.length === 0 ? (
        <p className="text-zinc-500 py-4">Chưa có đánh giá nào.</p>
      ) : (
        <div className="space-y-4">
          {reviews.map((review) => (
            <div key={review.id} className="border-b border-zinc-100 pb-4 last:border-0">
              <div className="flex items-center justify-between mb-2">
                <div className="flex items-center gap-2">
                  <div className="h-8 w-8 rounded-full bg-emerald-100 flex items-center justify-center text-emerald-700 font-bold text-xs">
                    {review.userName.charAt(0).toUpperCase()}
                  </div>
                  <span className="font-medium text-zinc-900">{review.userName}</span>
                </div>
                <span className="text-xs text-zinc-500">
                  {new Date(review.createdAt).toLocaleDateString("vi-VN")}
                </span>
              </div>
              <div className="flex mb-2">
                {[...Array(5)].map((_, i) => (
                  <Star
                    key={i}
                    className={clsx(
                      "h-4 w-4",
                      i < review.rating ? "fill-yellow-400 text-yellow-400" : "text-zinc-200"
                    )}
                  />
                ))}
              </div>
              <p className="text-zinc-600 text-sm">{review.comment}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

