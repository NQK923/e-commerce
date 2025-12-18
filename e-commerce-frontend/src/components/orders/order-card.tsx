'use client';

import Link from "next/link";
import React from "react";
import { Order } from "../../types/order";
import { formatCurrency, formatDate } from "../../utils/format";
import { Badge } from "../ui/badge";

export const OrderCard: React.FC<{ order: Order }> = ({ order }) => {
  const tone: "success" | "warning" | "danger" | "default" =
    order.status === "PAID"
      ? "success"
      : order.status === "SHIPPING"
        ? "warning"
        : order.status === "DELIVERED"
          ? "default"
          : order.status === "RETURNED"
            ? "default"
            : order.status === "CANCELLED"
              ? "danger"
              : "warning";
  return (
    <Link
      href={`/orders/${order.id}`}
      className="block rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md"
    >
      <div className="flex items-center justify-between">
        <div className="flex flex-col gap-1">
          <span className="text-xs uppercase text-zinc-500">Order</span>
          <span className="text-lg font-semibold text-black">#{order.id}</span>
          <span className="text-sm text-zinc-600">{formatDate(order.createdAt)}</span>
        </div>
        <div className="text-right">
          <Badge tone={tone}>{order.status}</Badge>
          <div className="mt-2 text-lg font-bold text-black">
            {formatCurrency(order.total, order.currency ?? "USD")}
          </div>
        </div>
      </div>
      <div className="mt-3 text-sm text-zinc-600">
        {order.items.length} items - Status: {order.status}
      </div>
    </Link>
  );
};
