'use client';

import React, { useState } from 'react';
import { Upload, Download, FileText, CheckCircle, XCircle, AlertCircle } from 'lucide-react';
import { Button } from '@/src/components/ui/button';
import { useAuth } from '@/src/store/auth-store';
import { useToast } from '@/src/components/ui/toast-provider';
import Link from 'next/link';

export default function BulkUploadPage() {
    const { user } = useAuth();
    const { addToast } = useToast();
    const [file, setFile] = useState<File | null>(null);
    const [uploading, setUploading] = useState(false);
    const [result, setResult] = useState<{
        success: boolean;
        message: string;
        totalRows: number;
        successCount: number;
        failureCount: number;
        errors?: string[];
    } | null>(null);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const selectedFile = e.target.files?.[0];
        if (selectedFile && selectedFile.type === 'text/csv') {
            setFile(selectedFile);
            setResult(null);
        } else {
            addToast('Vui lòng chọn file CSV', 'error');
        }
    };

    const handleUpload = async () => {
        if (!file) return;

        setUploading(true);
        const formData = new FormData();
        formData.append('file', file);

        try {
            const response = await fetch('/api/seller/products/bulk', {
                method: 'POST',
                body: formData,
                headers: {
                    // Token will be added by interceptor
                },
            });

            const data = await response.json();
            setResult(data);

            if (data.success) {
                addToast(`Tải lên thành công ${data.successCount} sản phẩm`, 'success');
            } else {
                addToast(data.message || 'Tải lên thất bại', 'error');
            }
        } catch (error) {
            addToast('Không thể tải lên file', 'error');
        } finally {
            setUploading(false);
        }
    };

    const downloadTemplate = () => {
        const csvContent = `name,description,price,currency,categoryId,quantity,tags
"Laptop Gaming","High performance gaming laptop",1500,USD,electronics,10,"gaming,laptop"
"Wireless Mouse","Ergonomic wireless mouse",25,USD,electronics,100,"accessories,mouse"
"USB Cable","USB-C to USB-A cable",10,USD,electronics,200,"cables,accessories"`;

        const blob = new Blob([csvContent], { type: 'text/csv' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'product-template.csv';
        link.click();
        URL.revokeObjectURL(url);
    };

    return (
        <div className="mx-auto max-w-4xl px-4 py-8">
            {/* Header */}
            <div className="mb-8">
                <h1 className="text-3xl font-bold text-zinc-900">Tải lên hàng loạt sản phẩm</h1>
                <p className="mt-2 text-zinc-600">
                    Tải lên nhiều sản phẩm cùng lúc bằng file CSV
                </p>
            </div>

            {/* Instructions */}
            <div className="mb-8 rounded-xl border border-blue-200 bg-blue-50 p-6">
                <div className="flex items-start gap-3">
                    <AlertCircle className="mt-0.5 shrink-0 text-blue-600" size={20} />
                    <div className="flex-1">
                        <h3 className="font-semibold text-blue-900">Hướng dẫn</h3>
                        <ol className="mt-2 space-y-1 text-sm text-blue-800">
                            <li>1. Tải xuống file mẫu CSV</li>
                            <li>2. Điền thông tin sản phẩm theo định dạng</li>
                            <li>3. Tải file lên hệ thống</li>
                        </ol>
                    </div>
                </div>
            </div>

            {/* Download Template */}
            <div className="mb-8 rounded-xl border border-zinc-200 bg-white p-6">
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-emerald-100">
                            <FileText className="text-emerald-600" size={24} />
                        </div>
                        <div>
                            <h3 className="font-semibold text-zinc-900">File mẫu CSV</h3>
                            <p className="text-sm text-zinc-600">
                                Tải xuống để xem định dạng chuẩn
                            </p>
                        </div>
                    </div>
                    <Button onClick={downloadTemplate} className="gap-2">
                        <Download size={16} />
                        Tải xuống mẫu
                    </Button>
                </div>

                {/* CSV Format Info */}
                <div className="mt-4 rounded-lg bg-zinc-50 p-4">
                    <p className="text-sm font-semibold text-zinc-700 mb-2">Định dạng CSV:</p>
                    <code className="block text-xs text-zinc-600 whitespace-pre-wrap font-mono">
                        name,description,price,currency,categoryId,quantity,tags
                    </code>
                </div>
            </div>

            {/* Upload Area */}
            <div className="mb-8 rounded-xl border-2 border-dashed border-zinc-300 bg-white p-8">
                <div className="text-center">
                    <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-zinc-100">
                        <Upload className="text-zinc-600" size={32} />
                    </div>
                    <h3 className="mt-4 font-semibold text-zinc-900">Tải lên file CSV</h3>
                    <p className="mt-1 text-sm text-zinc-600">
                        Kéo thả file hoặc click để chọn
                    </p>

                    <input
                        type="file"
                        accept=".csv"
                        onChange={handleFileChange}
                        className="hidden"
                        id="csv-upload"
                    />
                    <label htmlFor="csv-upload">
                        <Button className="mt-4" variant="outline" as="span">
                            Chọn file CSV
                        </Button>
                    </label>

                    {file && (
                        <div className="mt-4 inline-flex items-center gap-2 rounded-lg bg-emerald-50 px-4 py-2 text-sm text-emerald-700">
                            <FileText size={16} />
                            {file.name}
                        </div>
                    )}
                </div>
            </div>

            {/* Upload Button */}
            {file && (
                <div className="mb-8 flex justify-center">
                    <Button
                        onClick={handleUpload}
                        disabled={uploading}
                        className="w-full max-w-xs gap-2 bg-emerald-600 hover:bg-emerald-700"
                    >
                        {uploading ? (
                            <>Đang tải lên...</>
                        ) : (
                            <>
                                <Upload size={16} />
                                Tải lên và xử lý
                            </>
                        )}
                    </Button>
                </div>
            )}

            {/* Result */}
            {result && (
                <div className={`rounded-xl border p-6 ${result.success
                        ? 'border-emerald-200 bg-emerald-50'
                        : 'border-amber-200 bg-amber-50'
                    }`}>
                    <div className="flex items-start gap-3">
                        {result.success ? (
                            <CheckCircle className="shrink-0 text-emerald-600" size={24} />
                        ) : (
                            <XCircle className="shrink-0 text-amber-600" size={24} />
                        )}
                        <div className="flex-1">
                            <h3 className={`font-semibold ${result.success ? 'text-emerald-900' : 'text-amber-900'
                                }`}>
                                {result.message}
                            </h3>

                            <div className="mt-3 grid gap-2 text-sm">
                                <div className="flex justify-between">
                                    <span>Tổng số dòng:</span>
                                    <span className="font-semibold">{result.totalRows}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span>Thành công:</span>
                                    <span className="font-semibold text-emerald-600">{result.successCount}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span>Thất bại:</span>
                                    <span className="font-semibold text-red-600">{result.failureCount}</span>
                                </div>
                            </div>

                            {result.errors && result.errors.length > 0 && (
                                <div className="mt-4">
                                    <p className="text-sm font-semibold text-amber-800 mb-2">Lỗi:</p>
                                    <ul className="space-y-1 text-sm text-amber-700">
                                        {result.errors.map((error, idx) => (
                                            <li key={idx}>• {error}</li>
                                        ))}
                                    </ul>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            )}

            {/* Back Link */}
            <div className="mt-8 text-center">
                <Link href="/seller/products" className="text-sm text-emerald-600 hover:underline">
                    ← Quay lại danh sách sản phẩm
                </Link>
            </div>
        </div>
    );
}
