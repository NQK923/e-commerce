'use client';

import React, { Suspense, useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { Plus, Search, Package2, Trash2, Edit } from 'lucide-react';

import { productApi } from '@/src/api/productApi';
import { Button } from '@/src/components/ui/button';
import { Input } from '@/src/components/ui/input';
import { Spinner } from '@/src/components/ui/spinner';
import { Card } from '@/src/components/ui/card';
import { Product } from '@/src/types/product';
import { useRequireAuth } from '@/src/hooks/use-require-auth';
import { useToast } from '@/src/components/ui/toast-provider';
import { useDebounce } from '@/src/hooks/use-debounce';

const formatMoney = (amount: number, currency = "VND") => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency }).format(amount);
};

function SellerProductsContent() {
    const { user, initializing } = useRequireAuth('/login');
    const router = useRouter();
    const searchParams = useSearchParams();
    const { addToast } = useToast();

    const initialSearch = searchParams.get('search') || '';
    const initialPage = Number(searchParams.get('page')) || 0;
    const initialSize = Number(searchParams.get('size')) || 10;

    const [products, setProducts] = useState<Product[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [totalPages, setTotalPages] = useState(0);

    const [searchTerm, setSearchTerm] = useState(initialSearch);
    const [currentPage, setCurrentPage] = useState(initialPage);
    const [pageSize, setPageSize] = useState(initialSize);
    const [processingId, setProcessingId] = useState<string | null>(null);

    const debouncedSearchTerm = useDebounce(searchTerm, 500);

    const fetchProducts = React.useCallback(async () => {
        if (!user || !user.roles?.includes('SELLER')) return;

        setLoading(true);
        setError(null);
        try {
            const response = await productApi.list({
                search: debouncedSearchTerm || undefined,
                page: currentPage,
                size: pageSize,
                sort: 'createdAt,desc',
                includeOutOfStock: true,
            });
            setProducts(response.items);
            setTotalPages(response.totalPages || 0);
        } catch (err) {
            console.error('Failed to fetch seller products', err);
            setError('Không thể tải sản phẩm. Vui lòng thử lại.');
        } finally {
            setLoading(false);
        }
    }, [user, debouncedSearchTerm, currentPage, pageSize]);

    useEffect(() => {
        if (!initializing && user && user.roles?.includes('SELLER')) {
            fetchProducts();
        }
    }, [user, initializing, fetchProducts]);

    useEffect(() => {
        const params = new URLSearchParams();
        if (debouncedSearchTerm) params.set('search', debouncedSearchTerm);
        if (currentPage > 0) params.set('page', currentPage.toString());
        if (pageSize !== 10) params.set('size', pageSize.toString());
        router.replace(`/seller/products?${params.toString()}`, { scroll: false });
    }, [debouncedSearchTerm, currentPage, pageSize, router]);

    const handleEdit = (productId: string) => {
        router.push(`/seller/products/new?productId=${productId}`);
    };

    const handleRemove = async (product: Product) => {
        if (!confirm(`Bạn có chắc chắn muốn gỡ sản phẩm "${product.name}"? Thao tác này sẽ đặt số lượng sản phẩm về 0.`)) return;
        setProcessingId(product.id);
        try {
            await productApi.update(product.id, {
                name: product.name,
                description: product.description,
                price: product.price,
                currency: product.currency ?? "VND",
                quantity: 0, // Mark as out of stock
                categoryId: product.category,
                images: product.images?.map((img, idx) => ({
                    url: img.url,
                    primary: img.primary ?? idx === 0,
                })) ?? [],
            });
            addToast("Đã gỡ sản phẩm khỏi danh sách (số lượng đặt về 0)", "success");
            await fetchProducts(); // Refresh list
        } catch (err) {
            console.error("Failed to remove product", err);
            addToast("Lỗi khi gỡ sản phẩm", "error");
        } finally {
            setProcessingId(null);
        }
    };

    if (initializing || !user) {
        return (
            <div className="flex min-h-[60vh] flex-col items-center justify-center gap-3 text-sm text-zinc-600">
                <Spinner size="lg" />
                <span className="animate-pulse">Đang tải...</span>
            </div>
        );
    }

    if (!user.roles?.includes('SELLER')) {
        router.replace('/seller/register'); // Redirect if not a seller
        return null;
    }

    return (
        <div className="mx-auto max-w-7xl px-4 py-8 space-y-8 min-h-screen">
            <header className="flex items-center justify-between pb-6 border-b border-zinc-200">
                <h1 className="text-3xl font-bold text-zinc-900">Quản lý sản phẩm</h1>
                <Link href="/seller/products/new">
                    <Button className="bg-emerald-600 hover:bg-emerald-700">
                        <Plus size={18} className="mr-2" />
                        Thêm sản phẩm mới
                    </Button>
                </Link>
            </header>

            <div className="flex flex-col sm:flex-row gap-4 items-center justify-between">
                <div className="relative w-full sm:max-w-xs">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-zinc-400" />
                    <Input
                        placeholder="Tìm kiếm sản phẩm..."
                        value={searchTerm}
                        onChange={(e) => { setSearchTerm(e.target.value); setCurrentPage(0); }}
                        className="pl-9"
                    />
                </div>
                <div className="flex items-center gap-2 text-sm text-zinc-500">
                    <span>Hiển thị</span>
                    <select
                        value={pageSize}
                        onChange={(e) => { setPageSize(Number(e.target.value)); setCurrentPage(0); }}
                        className="rounded-md border border-zinc-200 bg-white p-2"
                    >
                        <option value={5}>5</option>
                        <option value={10}>10</option>
                        <option value={20}>20</option>
                    </select>
                    <span>sản phẩm mỗi trang</span>
                </div>
            </div>

            {loading ? (
                <div className="flex min-h-[300px] items-center justify-center">
                    <Spinner size="lg" />
                </div>
            ) : error ? (
                <div className="text-center text-red-600 p-8 border border-red-200 rounded-lg bg-red-50">
                    {error}
                </div>
            ) : (
                <Card className="overflow-hidden border-zinc-200 shadow-sm">
                    <div className="overflow-x-auto">
                        <table className="min-w-full text-left text-sm">
                            <thead className="bg-zinc-50 text-zinc-500">
                                <tr>
                                    <th className="px-6 py-3 font-semibold">Sản phẩm</th>
                                    <th className="px-6 py-3 font-semibold">Danh mục</th>
                                    <th className="px-6 py-3 font-semibold">Giá</th>
                                    <th className="px-6 py-3 font-semibold">Tồn kho</th>
                                    <th className="px-6 py-3 font-semibold">Hành động</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-zinc-100">
                                {products.length > 0 ? (
                                    products.map((product) => (
                                        <tr key={product.id} className="hover:bg-zinc-50 transition-colors">
                                            <td className="px-6 py-3 max-w-xs">
                                                <div className="font-semibold text-zinc-900 truncate" title={product.name}>{product.name}</div>
                                                <div className="text-xs text-zinc-500 truncate">{product.description}</div>
                                            </td>
                                            <td className="px-6 py-3">
                                                <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-zinc-100 text-zinc-800">
                                                    {product.category ?? "Khác"}
                                                </span>
                                            </td>
                                            <td className="px-6 py-3 font-medium text-zinc-700">{formatMoney(product.price, product.currency)}</td>
                                            <td className="px-6 py-3">
                                                <span className={!product.stock ? "text-red-600 font-bold bg-red-50 px-2 py-1 rounded-full text-xs" : "text-zinc-700"}>
                                                    {product.stock ? product.stock : "Hết hàng"}
                                                </span>
                                            </td>
                                            <td className="px-6 py-3">
                                                <div className="flex gap-2">
                                                    <Button
                                                        size="sm"
                                                        variant="outline"
                                                        onClick={() => handleEdit(product.id)}
                                                        className="h-8 px-3 text-zinc-600 hover:text-emerald-700 hover:border-emerald-200 hover:bg-emerald-50"
                                                    >
                                                        <Edit size={16} className="mr-1.5" /> Sửa
                                                    </Button>
                                                    <Button
                                                        size="sm"
                                                        variant="ghost"
                                                        className="h-8 w-8 p-0 text-zinc-400 hover:text-red-600 hover:bg-red-50"
                                                        onClick={() => handleRemove(product)}
                                                        disabled={processingId === product.id}
                                                        title="Gỡ sản phẩm"
                                                    >
                                                        {processingId === product.id ? <Spinner size="sm" /> : <Trash2 size={16} />}
                                                    </Button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))
                                ) : (
                                    <tr>
                                        <td colSpan={5} className="px-6 py-12 text-center text-zinc-500 flex flex-col items-center justify-center">
                                            <Package2 size={32} className="mb-2 opacity-20" />
                                            Không tìm thấy sản phẩm nào.
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </Card>
            )}

            {totalPages > 1 && (
                <div className="flex items-center justify-center gap-2 mt-8">
                    <Button 
                        variant="outline" 
                        disabled={currentPage === 0} 
                        onClick={() => setCurrentPage(prev => prev - 1)}
                    >
                        Trước
                    </Button>
                    <div className="flex items-center gap-1 px-4 text-sm font-medium">
                        Trang {currentPage + 1} / {totalPages}
                    </div>
                    <Button 
                        variant="outline" 
                        disabled={currentPage + 1 >= totalPages} 
                        onClick={() => setCurrentPage(prev => prev + 1)}
                    >
                        Sau
                    </Button>
                </div>
            )}
        </div>
    );
}

export default function SellerProductsPage() {
    return (
        <Suspense fallback={
            <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
                <Spinner />
                Đang tải sản phẩm...
            </div>
        }>
            <SellerProductsContent />
        </Suspense>
    );
}
