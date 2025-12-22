'use client';

import React, { useEffect, useMemo, useState } from "react";
import { CheckCircle2, Flag, MessageSquare, Shield, Star } from "lucide-react";
import clsx from "clsx";

import { productApi, Review } from "@/src/api/productApi";
import { orderApi } from "@/src/api/orderApi";
import { ApiError } from "@/src/lib/api-client";
import { useAuth } from "@/src/store/auth-store";
import { Button } from "@/src/components/ui/button";
import { Badge } from "@/src/components/ui/badge";
import { useToast } from "@/src/components/ui/toast-provider";
import { Spinner } from "@/src/components/ui/spinner";
import { useTranslation } from "@/src/providers/language-provider";

interface ProductReviewsProps {
  productId: string;
  sellerId?: string;
}

export function ProductReviews({ productId, sellerId }: ProductReviewsProps) {
  const { user } = useAuth();
  const { addToast } = useToast();
  const { t } = useTranslation();

  const reportReasons = [
    { value: "INAPPROPRIATE", label: t.product.reviews.report_reasons.inappropriate },
    { value: "FAKE", label: t.product.reviews.report_reasons.fake },
    { value: "SCAM", label: t.product.reviews.report_reasons.scam },
    { value: "OTHER", label: t.product.reviews.report_reasons.other },
  ];

  const [reviews, setReviews] = useState<Review[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [hasPurchased, setHasPurchased] = useState(false);
  const [editingReviewId, setEditingReviewId] = useState<string | null>(null);
  const [reportingReviewId, setReportingReviewId] = useState<string | null>(null);
  const [respondingReviewId, setRespondingReviewId] = useState<string | null>(null);
  const [actionInProgress, setActionInProgress] = useState<string | null>(null);

  // Form state
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");
  const [reportReason, setReportReason] = useState(reportReasons[0].value);
  const [reportDescription, setReportDescription] = useState("");
  const [responseText, setResponseText] = useState("");

  const fetchReviews = React.useCallback(async () => {
    setLoading(true);
    try {
      const resp = await productApi.fetchReviews(productId);
      setReviews(resp.items);
    } catch (error) {
      console.error("Failed to load reviews", error);
      addToast(t.product.load_failed, "error");
    } finally {
      setLoading(false);
    }
  }, [addToast, productId, t]); // Added t to dependency

  const checkIfUserPurchasedProduct = React.useCallback(async () => {
    if (!user?.id) {
      setHasPurchased(false);
      return;
    }
    try {
      const userOrdersResponse = await orderApi.list({ page: 0, size: 50, sort: "createdAt,desc" });
      const purchased = userOrdersResponse.items.some(
        (order) =>
        (order.status === "PAID" || order.status === "SHIPPING" || order.status === "DELIVERED" || order.status === "RETURNED") &&
          order.items.some((item) => item.productId === productId),
      );
      setHasPurchased(purchased);
    } catch (error) {
      console.error("Failed to check purchase status", error);
      setHasPurchased(false);
    }
  }, [productId, user?.id]);

  useEffect(() => {
    fetchReviews();
    checkIfUserPurchasedProduct();
  }, [fetchReviews, checkIfUserPurchasedProduct]);

  const userHasReview = useMemo(
    () => !!(user?.id && reviews.some((r) => r.userId === user.id)),
    [reviews, user?.id],
  );

  const resetForm = () => {
    setRating(5);
    setComment("");
    setEditingReviewId(null);
    setShowForm(false);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) {
      addToast(t.product.reviews.login_required, "error");
      return;
    }
    if (!editingReviewId && !hasPurchased) {
      addToast(t.product.reviews.buyer_only, "error");
      return;
    }
    setSubmitting(true);
    try {
      if (editingReviewId) {
        await productApi.updateReview(productId, editingReviewId, {
          userId: user.id,
          rating,
          comment,
        });
        addToast(t.product.reviews.messages.updated, "success");
      } else {
        await productApi.addReview(productId, {
          userId: user.id,
          userName: user.displayName || user.email || "User",
          rating,
          comment,
        });
        addToast(t.product.reviews.messages.submitted, "success");
      }
      resetForm();
      await fetchReviews();
    } catch (error) {
      console.error("Failed to submit review", error);
      if (error instanceof ApiError && error.status === 429) {
        addToast(t.product.reviews.messages.too_fast, "error");
      } else {
        addToast(t.common.error, "error");
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (reviewId: string) => {
    if (!user) {
      addToast(t.product.reviews.login_required, "error");
      return;
    }
    setActionInProgress(reviewId);
    try {
      await productApi.deleteReview(productId, reviewId, { userId: user.id });
      setReviews((prev) => prev.filter((r) => r.id !== reviewId));
      if (editingReviewId === reviewId) {
        resetForm();
      }
      addToast(t.product.reviews.messages.deleted, "success");
    } catch (error) {
      console.error("Failed to delete review", error);
      addToast(t.common.error, "error");
    } finally {
      setActionInProgress(null);
    }
  };

  const handleReport = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user || !reportingReviewId) {
      addToast(t.product.reviews.login_required, "error");
      return;
    }
    setActionInProgress(reportingReviewId);
    try {
      await productApi.reportReview(productId, reportingReviewId, {
        reporterUserId: user.id,
        reason: reportReason,
        description: reportDescription,
      });
      addToast(t.product.reviews.messages.reported, "success");
      setReportingReviewId(null);
      setReportDescription("");
    } catch (error) {
      console.error("Failed to report review", error);
      addToast(t.common.error, "error");
    } finally {
      setActionInProgress(null);
    }
  };

  const handleRespond = async () => {
    if (!user || !respondingReviewId || !sellerId || user.id !== sellerId) {
      return;
    }
    setActionInProgress(respondingReviewId);
    try {
      await productApi.respondToReview(productId, respondingReviewId, {
        sellerId,
        response: responseText,
      });
      addToast(t.product.reviews.messages.response_posted, "success");
      setRespondingReviewId(null);
      setResponseText("");
      await fetchReviews();
    } catch (error) {
      console.error("Failed to respond to review", error);
      addToast(t.common.error, "error");
    } finally {
      setActionInProgress(null);
    }
  };

  const startEdit = (review: Review) => {
    setEditingReviewId(review.id);
    setRating(review.rating);
    setComment(review.comment);
    setShowForm(true);
  };

  const startRespond = (review: Review) => {
    setRespondingReviewId(review.id);
    setResponseText(review.sellerResponse ?? "");
    setReportingReviewId(null);
  };

  const startReport = (review: Review) => {
    setReportingReviewId(review.id);
    setReportReason(reportReasons[0].value);
    setReportDescription("");
    setRespondingReviewId(null);
  };

  return (
    <div className="space-y-6 border-t border-zinc-100 pt-8">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-xl font-bold text-zinc-900">{t.product.reviews.title} ({reviews.length})</h3>
          <p className="text-sm text-zinc-500">{t.product.reviews.subtitle}</p>
        </div>
        {user && !showForm && (hasPurchased || userHasReview) && (
          <Button variant="outline" onClick={() => setShowForm(true)}>
            {userHasReview ? t.product.reviews.edit : t.product.reviews.write}
          </Button>
        )}
      </div>

      {!user ? (
        <p className="py-4 text-zinc-500">{t.product.reviews.login_required}</p>
      ) : !hasPurchased && !userHasReview && !showForm ? (
        <p className="py-4 text-zinc-500">{t.product.reviews.buyer_only}</p>
      ) : null}

      {showForm && (
        <form onSubmit={handleSubmit} className="space-y-4 rounded-xl border border-zinc-200 bg-zinc-50 p-6">
          <div>
            <label className="mb-2 block text-sm font-medium text-zinc-700">
              {editingReviewId ? t.product.reviews.update_review : t.product.reviews.your_review}
            </label>
            <div className="flex gap-1">
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  key={star}
                  type="button"
                  onClick={() => setRating(star)}
                  className="focus:outline-none"
                  aria-label={`${t.product.rating} ${star}`}
                >
                  <Star
                    className={clsx(
                      "h-6 w-6 transition-colors",
                      star <= rating ? "fill-yellow-400 text-yellow-400" : "text-zinc-300",
                    )}
                  />
                </button>
              ))}
            </div>
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-zinc-700">{t.product.reviews.comment_label}</label>
            <textarea
              className="w-full min-h-[100px] rounded-lg border border-zinc-300 p-3 outline-none focus:ring-2 focus:ring-emerald-500"
              placeholder={t.product.reviews.comment_placeholder}
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              required
            />
          </div>
          <div className="flex justify-end gap-3">
            <Button type="button" variant="ghost" onClick={resetForm}>
              {t.common.cancel}
            </Button>
            <Button type="submit" disabled={submitting} className="bg-emerald-600 hover:bg-emerald-700">
              {submitting ? <Spinner className="mr-2 h-4 w-4" /> : null}
              {editingReviewId ? t.common.save_changes : t.common.submit}
            </Button>
          </div>
        </form>
      )}

      {loading ? (
        <div className="flex justify-center py-8">
          <Spinner />
        </div>
      ) : reviews.length === 0 ? (
        <p className="py-4 text-zinc-500">{t.product.reviews.no_reviews}</p>
      ) : (
        <div className="space-y-4">
          {reviews.map((review) => {
            const isOwner = user?.id === review.userId;
            const isSeller = sellerId && user?.id === sellerId;
            return (
              <div key={review.id} className="space-y-2 border-b border-zinc-100 pb-4 last:border-0">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="flex h-9 w-9 items-center justify-center rounded-full bg-emerald-100 text-sm font-bold text-emerald-700">
                      {review.userName.charAt(0).toUpperCase()}
                    </div>
                    <div>
                      <div className="flex flex-wrap items-center gap-2">
                        <span className="font-medium text-zinc-900">{review.userName}</span>
                        {review.verifiedPurchase ? (
                          <Badge tone="success" className="inline-flex items-center gap-1">
                            <Shield className="h-3 w-3" />
                            {t.product.reviews.verified_buyer}
                          </Badge>
                        ) : null}
                        {review.abuseReportCount && review.abuseReportCount > 0 ? (
                          <Badge tone="warning" className="inline-flex items-center gap-1">
                            <Flag className="h-3 w-3" />
                            {review.abuseReportCount} {t.common.report}
                          </Badge>
                        ) : null}
                      </div>
                      <p className="text-xs text-zinc-500">
                        {new Date(review.createdAt).toLocaleDateString("vi-VN")}
                        {review.updatedAt && review.updatedAt !== review.createdAt
                          ? ` · ${t.common.updated} ${new Date(review.updatedAt).toLocaleDateString("vi-VN")}`
                          : ""}
                      </p>
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-1">
                  {[...Array(5)].map((_, i) => (
                    <Star
                      key={i}
                      className={clsx("h-4 w-4", i < review.rating ? "fill-yellow-400 text-yellow-400" : "text-zinc-200")}
                    />
                  ))}
                </div>

                <p className="text-sm text-zinc-700">{review.comment}</p>

                {review.sellerResponse ? (
                  <div className="rounded-lg border border-emerald-100 bg-emerald-50 p-3 text-sm text-emerald-800">
                    <div className="mb-1 flex items-center gap-2 font-semibold">
                      <MessageSquare className="h-4 w-4" /> {t.product.reviews.seller_response}
                      {review.sellerRespondedAt ? (
                        <span className="text-xs font-normal text-emerald-700/80">
                          {new Date(review.sellerRespondedAt).toLocaleDateString("vi-VN")}
                        </span>
                      ) : null}
                    </div>
                    <p className="text-emerald-900">{review.sellerResponse}</p>
                  </div>
                ) : null}

                <div className="flex flex-wrap items-center gap-3 text-sm text-zinc-500">
                  {isOwner ? (
                    <>
                      <button
                        className="hover:text-emerald-600"
                        type="button"
                        onClick={() => startEdit(review)}
                        disabled={actionInProgress === review.id}
                      >
                        {t.common.edit}
                      </button>
                      <span className="text-zinc-300">•</span>
                      <button
                        className="hover:text-red-600"
                        type="button"
                        onClick={() => handleDelete(review.id)}
                        disabled={actionInProgress === review.id}
                      >
                        {actionInProgress === review.id ? t.common.removing : t.common.delete}
                      </button>
                    </>
                  ) : null}

                  {user && !isOwner ? (
                    <>
                      <span className="text-zinc-300">•</span>
                      <button
                        className="flex items-center gap-1 hover:text-amber-600"
                        type="button"
                        onClick={() => startReport(review)}
                        disabled={actionInProgress === review.id}
                      >
                        <Flag className="h-3 w-3" />
                        {t.product.reviews.report_abuse}
                      </button>
                    </>
                  ) : null}

                  {isSeller && review.userId !== sellerId ? (
                    <>
                      <span className="text-zinc-300">•</span>
                      <button
                        className="flex items-center gap-1 hover:text-emerald-700"
                        type="button"
                        onClick={() => startRespond(review)}
                        disabled={actionInProgress === review.id}
                      >
                        <CheckCircle2 className="h-3 w-3" />
                        {t.common.respond}
                      </button>
                    </>
                  ) : null}
                </div>

                {reportingReviewId === review.id ? (
                  <form onSubmit={handleReport} className="space-y-3 rounded-lg border border-amber-200 bg-amber-50 p-3">
                    <div className="flex flex-wrap gap-2">
                      {reportReasons.map((reason) => (
                        <button
                          key={reason.value}
                          type="button"
                          onClick={() => setReportReason(reason.value)}
                          className={clsx(
                            "rounded-full px-3 py-1 text-xs font-semibold transition",
                            reportReason === reason.value
                              ? "bg-amber-600 text-white shadow"
                              : "bg-white text-amber-700 ring-1 ring-amber-200 hover:bg-amber-100",
                          )}
                        >
                          {reason.label}
                        </button>
                      ))}
                    </div>
                    <textarea
                      className="w-full rounded-lg border border-amber-200 p-2 text-sm outline-none focus:ring-2 focus:ring-amber-500"
                      placeholder={t.product.reviews.report_placeholder}
                      value={reportDescription}
                      onChange={(e) => setReportDescription(e.target.value)}
                    />
                    <div className="flex justify-end gap-2">
                      <Button type="button" variant="ghost" onClick={() => setReportingReviewId(null)}>
                        {t.common.cancel}
                      </Button>
                      <Button type="submit" disabled={actionInProgress === review.id} className="bg-amber-600 hover:bg-amber-700">
                        {actionInProgress === review.id ? <Spinner className="mr-2 h-4 w-4" /> : null}
                        {t.product.reviews.submit_report}
                      </Button>
                    </div>
                  </form>
                ) : null}

                {respondingReviewId === review.id ? (
                  <div className="space-y-2 rounded-lg border border-emerald-200 bg-emerald-50 p-3">
                    <textarea
                      className="w-full rounded-lg border border-emerald-200 p-2 text-sm outline-none focus:ring-2 focus:ring-emerald-500"
                      placeholder={t.product.reviews.comment_placeholder}
                      value={responseText}
                      onChange={(e) => setResponseText(e.target.value)}
                    />
                    <div className="flex justify-end gap-2">
                      <Button type="button" variant="ghost" onClick={() => setRespondingReviewId(null)}>
                        {t.common.cancel}
                      </Button>
                      <Button
                        type="button"
                        disabled={actionInProgress === review.id}
                        className="bg-emerald-600 hover:bg-emerald-700"
                        onClick={handleRespond}
                      >
                        {actionInProgress === review.id ? <Spinner className="mr-2 h-4 w-4" /> : null}
                        {t.common.submit}
                      </Button>
                    </div>
                  </div>
                ) : null}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

