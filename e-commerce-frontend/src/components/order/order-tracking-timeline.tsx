'use client';

import React from 'react';
import { Check, Circle, Package, Truck, Home, XCircle } from 'lucide-react';

export type OrderStatus = 'PENDING' | 'PAID' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED' | 'RETURNED';

interface OrderTrackingTimelineProps {
    status: OrderStatus;
    createdAt?: string;
    paidAt?: string;
    shippedAt?: string;
    deliveredAt?: string;
    cancelledAt?: string;
    trackingNumber?: string;
}

interface TimelineStep {
    label: string;
    icon: React.ReactNode;
    completed: boolean;
    active: boolean;
    timestamp?: string;
    description?: string;
}

export function OrderTrackingTimeline({
    status,
    createdAt,
    paidAt,
    shippedAt,
    deliveredAt,
    cancelledAt,
    trackingNumber,
}: OrderTrackingTimelineProps) {

    const isCancelled = status === 'CANCELLED';
    const isReturned = status === 'RETURNED';

    const steps: TimelineStep[] = [
        {
            label: 'Đơn hàng đã tạo',
            icon: <Package size={20} />,
            completed: true,
            active: status === 'PENDING',
            timestamp: createdAt,
            description: 'Đơn hàng đã được tạo thành công',
        },
        {
            label: 'Đã thanh toán',
            icon: <Check size={20} />,
            completed: ['PAID', 'SHIPPED', 'DELIVERED', 'RETURNED'].includes(status),
            active: status === 'PAID',
            timestamp: paidAt,
            description: 'Thanh toán đã được xác nhận',
        },
        {
            label: 'Đang giao hàng',
            icon: <Truck size={20} />,
            completed: ['SHIPPED', 'DELIVERED', 'RETURNED'].includes(status),
            active: status === 'SHIPPED',
            timestamp: shippedAt,
            description: trackingNumber ? `Mã vận đơn: ${trackingNumber}` : 'Đơn hàng đang được vận chuyển',
        },
        {
            label: 'Đã giao',
            icon: <Home size={20} />,
            completed: ['DELIVERED', 'RETURNED'].includes(status),
            active: status === 'DELIVERED',
            timestamp: deliveredAt,
            description: 'Đơn hàng đã được giao thành công',
        },
    ];

    if (isCancelled) {
        return (
            <div className="rounded-xl border border-red-200 bg-red-50 p-6">
                <div className="flex items-center gap-3">
                    <div className="flex h-12 w-12 items-center justify-center rounded-full bg-red-100 text-red-600">
                        <XCircle size={24} />
                    </div>
                    <div>
                        <h3 className="font-semibold text-red-900">Đơn hàng đã bị hủy</h3>
                        {cancelledAt && (
                            <p className="text-sm text-red-600 mt-1">
                                {new Date(cancelledAt).toLocaleString('vi-VN')}
                            </p>
                        )}
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="rounded-xl border border-zinc-200 bg-white p-6">
            <h3 className="text-lg font-semibold text-zinc-900 mb-6">Theo dõi đơn hàng</h3>

            <div className="relative">
                {/* Vertical line */}
                <div className="absolute left-6 top-0 bottom-0 w-0.5 bg-zinc-200" />

                <div className="space-y-8">
                    {steps.map((step, index) => (
                        <div key={index} className="relative flex gap-4">
                            {/* Icon */}
                            <div
                                className={`
                  relative z-10 flex h-12 w-12 shrink-0 items-center justify-center rounded-full border-2 transition-all
                  ${step.completed
                                        ? 'border-emerald-500 bg-emerald-500 text-white shadow-lg shadow-emerald-500/30'
                                        : step.active
                                            ? 'border-emerald-500 bg-white text-emerald-600 animate-pulse'
                                            : 'border-zinc-300 bg-white text-zinc-400'
                                    }
                `}
                            >
                                {step.completed ? <Check size={20} className="font-bold" /> : step.icon}
                            </div>

                            {/* Content */}
                            <div className="flex-1 pt-1">
                                <div className="flex items-center gap-2">
                                    <h4
                                        className={`font-semibold ${step.completed || step.active ? 'text-zinc-900' : 'text-zinc-400'
                                            }`}
                                    >
                                        {step.label}
                                    </h4>
                                    {step.active && (
                                        <span className="rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-medium text-emerald-700">
                                            Hiện tại
                                        </span>
                                    )}
                                </div>

                                {step.description && (step.completed || step.active) && (
                                    <p className="mt-1 text-sm text-zinc-600">{step.description}</p>
                                )}

                                {step.timestamp && (step.completed || step.active) && (
                                    <p className="mt-1 text-xs text-zinc-500">
                                        {new Date(step.timestamp).toLocaleString('vi-VN', {
                                            year: 'numeric',
                                            month: 'long',
                                            day: 'numeric',
                                            hour: '2-digit',
                                            minute: '2-digit',
                                        })}
                                    </p>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            {isReturned && (
                <div className="mt-6 rounded-lg border border-amber-200 bg-amber-50 p-4">
                    <div className="flex items-center gap-2 text-amber-800">
                        <Circle size={16} />
                        <span className="font-medium">Đơn hàng đã được hoàn trả</span>
                    </div>
                </div>
            )}
        </div>
    );
}
