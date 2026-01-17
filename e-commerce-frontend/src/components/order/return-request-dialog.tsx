'use client';

import React, { useState } from 'react';
import { X, AlertCircle } from 'lucide-react';
import { Button } from '@/src/components/ui/button';
import { Input } from '@/src/components/ui/input';
import { useToast } from '@/src/components/ui/toast-provider';
import { orderApi } from '@/src/api/orderApi';

interface ReturnRequestDialogProps {
    orderId: string;
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
}

const RETURN_REASONS = [
    { value: 'DEFECTIVE', label: 'Sản phẩm bị lỗi/hư hỏng' },
    { value: 'WRONG_ITEM', label: 'Giao sai sản phẩm' },
    { value: 'NOT_AS_DESCRIBED', label: 'Không đúng mô tả' },
    { value: 'CHANGED_MIND', label: 'Đổi ý không muốn mua' },
    { value: 'OTHER', label: 'Lý do khác' },
];

export function ReturnRequestDialog({ orderId, isOpen, onClose, onSuccess }: ReturnRequestDialogProps) {
    const { addToast } = useToast();
    const [loading, setLoading] = useState(false);
    const [reason, setReason] = useState('DEFECTIVE');
    const [description, setDescription] = useState('');

    if (!isOpen) return null;

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!description.trim()) {
            addToast('Vui lòng nhập mô tả chi tiết', 'error');
            return;
        }

        setLoading(true);
        try {
            await orderApi.requestReturn(orderId, { reason, note: description });
            addToast('Yêu cầu hoàn trả đã được gửi', 'success');
            onSuccess();
            onClose();
        } catch (error) {
            const message = error instanceof Error ? error.message : 'Không thể gửi yêu cầu hoàn trả';
            addToast(message, 'error');
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            {/* Overlay */}
            <div
                className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm"
                onClick={onClose}
            />

            {/* Dialog */}
            <div className="fixed left-1/2 top-1/2 z-50 w-full max-w-md -translate-x-1/2 -translate-y-1/2 rounded-2xl border border-zinc-200 bg-white p-6 shadow-2xl">
                {/* Header */}
                <div className="mb-6 flex items-center justify-between">
                    <h2 className="text-xl font-bold text-zinc-900">Yêu cầu hoàn trả</h2>
                    <button
                        onClick={onClose}
                        className="rounded-lg p-2 text-zinc-500 transition-colors hover:bg-zinc-100 hover:text-zinc-900"
                    >
                        <X size={20} />
                    </button>
                </div>

                {/* Alert */}
                <div className="mb-6 flex gap-3 rounded-lg border border-amber-200 bg-amber-50 p-4">
                    <AlertCircle size={20} className="shrink-0 text-amber-600" />
                    <p className="text-sm text-amber-800">
                        Yêu cầu hoàn trả sẽ được xem xét trong vòng 24-48 giờ. Vui lòng cung cấp thông tin chi tiết để hỗ trợ nhanh chóng.
                    </p>
                </div>

                {/* Form */}
                <form onSubmit={handleSubmit} className="space-y-4">
                    {/* Reason */}
                    <div>
                        <label className="mb-2 block text-sm font-semibold text-zinc-700">
                            Lý do hoàn trả <span className="text-red-500">*</span>
                        </label>
                        <select
                            value={reason}
                            onChange={(e) => setReason(e.target.value)}
                            className="w-full rounded-lg border border-zinc-300 bg-white px-4 py-2.5 text-sm outline-none transition-colors focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
                            required
                        >
                            {RETURN_REASONS.map((r) => (
                                <option key={r.value} value={r.value}>
                                    {r.label}
                                </option>
                            ))}
                        </select>
                    </div>

                    {/* Description */}
                    <div>
                        <label className="mb-2 block text-sm font-semibold text-zinc-700">
                            Mô tả chi tiết <span className="text-red-500">*</span>
                        </label>
                        <textarea
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            className="w-full rounded-lg border border-zinc-300 bg-white px-4 py-2.5 text-sm outline-none transition-colors focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
                            rows={4}
                            placeholder="Mô tả vấn đề gặp phải với sản phẩm..."
                            required
                            maxLength={500}
                        />
                        <p className="mt-1 text-xs text-zinc-500">{description.length}/500 ký tự</p>
                    </div>

                    {/* Actions */}
                    <div className="flex gap-3 pt-2">
                        <Button
                            type="button"
                            variant="outline"
                            className="flex-1"
                            onClick={onClose}
                            disabled={loading}
                        >
                            Hủy
                        </Button>
                        <Button
                            type="submit"
                            className="flex-1 bg-emerald-600 hover:bg-emerald-700"
                            disabled={loading}
                        >
                            {loading ? 'Đang gửi...' : 'Gửi yêu cầu'}
                        </Button>
                    </div>
                </form>
            </div>
        </>
    );
}
