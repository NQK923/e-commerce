"use client";

import { useEffect, useState } from "react";
import { flashSaleApi, FlashSale } from "@/src/api/flashSaleApi";
import { Button } from "@/src/components/ui/button";
import { Card } from "@/src/components/ui/card";
import { Badge } from "@/src/components/ui/badge";
import { formatCurrency } from "@/src/utils/format";
import { Plus, ArrowLeft, Calendar, Package, Tag, Clock } from "lucide-react";

export default function AdminFlashSalesPage() {
    const [view, setView] = useState<'list' | 'create'>('list');
    const [sales, setSales] = useState<FlashSale[]>([]);
    const [loading, setLoading] = useState(false);

    // Form State
    const [form, setForm] = useState({
        productId: "",
        price: 0,
        currency: "USD",
        originalPrice: 0,
        originalCurrency: "USD",
        startTime: "",
        endTime: "",
        totalQuantity: 0
    });
    const [message, setMessage] = useState<{ type: 'success' | 'error', text: string } | null>(null);

    // Fetch Data
    useEffect(() => {
        if (view === 'list') {
            loadSales();
        }
    }, [view]);

    const loadSales = async () => {
        setLoading(true);
        try {
            const data = await flashSaleApi.listAllAdmin();
            // Sort by status (Active first) then startTime
            const sorted = data.sort((a, b) => {
                if (a.status === 'ACTIVE' && b.status !== 'ACTIVE') return -1;
                if (b.status === 'ACTIVE' && a.status !== 'ACTIVE') return 1;
                return new Date(b.startTime).getTime() - new Date(a.startTime).getTime();
            });
            setSales(sorted);
        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    const handleCreateSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setMessage(null);
        
        try {
            await flashSaleApi.create({
                ...form,
                startTime: new Date(form.startTime).toISOString(),
                endTime: new Date(form.endTime).toISOString()
            });
            setMessage({ type: 'success', text: "Tạo chiến dịch Flash Sale thành công!" });
            setTimeout(() => {
                setView('list');
                setForm({
                    productId: "",
                    price: 0,
                    currency: "USD",
                    originalPrice: 0,
                    originalCurrency: "USD",
                    startTime: "",
                    endTime: "",
                    totalQuantity: 0
                });
                setMessage(null);
            }, 1500);
        } catch (e: unknown) {
            let errorMessage = "Lỗi không xác định";
            if (e instanceof Error) errorMessage = e.message;
            else if (typeof e === 'string') errorMessage = e;
            setMessage({ type: 'error', text: "Lỗi: " + errorMessage });
        } finally {
            setLoading(false);
        }
    };

    const getStatusColor = (status: string) => {
        switch (status) {
            case 'ACTIVE': return 'bg-emerald-100 text-emerald-700 border-emerald-200';
            case 'PENDING': return 'bg-blue-100 text-blue-700 border-blue-200';
            case 'ENDED': return 'bg-zinc-100 text-zinc-600 border-zinc-200';
            case 'CANCELLED': return 'bg-red-100 text-red-700 border-red-200';
            default: return 'bg-zinc-100 text-zinc-600';
        }
    };

    if (view === 'create') {
        return (
            <div className="space-y-6">
                <div className="flex items-center gap-4">
                    <Button variant="ghost" onClick={() => setView('list')}>
                        <ArrowLeft size={20} className="mr-2" /> Quay lại
                    </Button>
                    <h1 className="text-2xl font-bold">Tạo chiến dịch Flash Sale mới</h1>
                </div>

                {message && (
                    <div className={`p-4 rounded-lg border ${message.type === 'success' ? 'bg-emerald-50 border-emerald-200 text-emerald-700' : 'bg-red-50 border-red-200 text-red-700'}`}>
                        {message.text}
                    </div>
                )}

                <Card className="p-6 max-w-3xl mx-auto shadow-md">
                    <form onSubmit={handleCreateSubmit} className="space-y-6">
                        <div className="grid gap-6 md:grid-cols-2">
                            <div className="col-span-2">
                                <label className="block text-sm font-medium mb-1.5 text-zinc-700">Mã sản phẩm (ID)</label>
                                <div className="relative">
                                    <Package className="absolute left-3 top-2.5 h-4 w-4 text-zinc-400" />
                                    <input 
                                        className="flex h-10 w-full rounded-md border border-zinc-300 bg-white pl-10 pr-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all" 
                                        value={form.productId} 
                                        onChange={e => setForm({...form, productId: e.target.value})} 
                                        required 
                                        placeholder="Nhập UUID sản phẩm..."
                                    />
                                </div>
                            </div>
                            
                            {/* Pricing */}
                            <div className="space-y-4 p-4 bg-zinc-50 rounded-lg border border-zinc-100">
                                <h3 className="font-semibold text-zinc-700 flex items-center gap-2">
                                    <Tag size={16} /> Giá khuyến mãi
                                </h3>
                                <div>
                                    <label className="block text-xs font-medium mb-1 text-zinc-500">Số tiền</label>
                                    <input 
                                        type="number" 
                                        className="flex h-10 w-full rounded-md border border-zinc-300 px-3 py-2 text-sm focus:ring-emerald-500 focus:border-emerald-500" 
                                        value={form.price} 
                                        onChange={e => setForm({...form, price: Number(e.target.value)})} 
                                        required 
                                    />
                                </div>
                                <div>
                                    <label className="block text-xs font-medium mb-1 text-zinc-500">Đơn vị tiền tệ</label>
                                    <input 
                                        className="flex h-10 w-full rounded-md border border-zinc-300 px-3 py-2 text-sm focus:ring-emerald-500 focus:border-emerald-500" 
                                        value={form.currency} 
                                        onChange={e => setForm({...form, currency: e.target.value})} 
                                        required 
                                    />
                                </div>
                            </div>

                            <div className="space-y-4 p-4 bg-zinc-50 rounded-lg border border-zinc-100">
                                <h3 className="font-semibold text-zinc-700 flex items-center gap-2">
                                    <Tag size={16} /> Giá gốc
                                </h3>
                                <div>
                                    <label className="block text-xs font-medium mb-1 text-zinc-500">Số tiền</label>
                                    <input 
                                        type="number" 
                                        className="flex h-10 w-full rounded-md border border-zinc-300 px-3 py-2 text-sm focus:ring-emerald-500 focus:border-emerald-500" 
                                        value={form.originalPrice} 
                                        onChange={e => setForm({...form, originalPrice: Number(e.target.value)})} 
                                        required 
                                    />
                                </div>
                                <div>
                                    <label className="block text-xs font-medium mb-1 text-zinc-500">Đơn vị tiền tệ</label>
                                    <input 
                                        className="flex h-10 w-full rounded-md border border-zinc-300 px-3 py-2 text-sm focus:ring-emerald-500 focus:border-emerald-500" 
                                        value={form.originalCurrency} 
                                        onChange={e => setForm({...form, originalCurrency: e.target.value})} 
                                        required 
                                    />
                                </div>
                            </div>

                            {/* Quantity & Time */}
                            <div className="col-span-2 grid grid-cols-3 gap-4">
                                <div>
                                    <label className="block text-sm font-medium mb-1.5 text-zinc-700">Tổng số lượng</label>
                                    <input 
                                        type="number" 
                                        className="flex h-10 w-full rounded-md border border-zinc-300 px-3 py-2 text-sm focus:ring-emerald-500 focus:border-emerald-500" 
                                        value={form.totalQuantity} 
                                        onChange={e => setForm({...form, totalQuantity: Number(e.target.value)})} 
                                        required 
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium mb-1.5 text-zinc-700">Thời gian bắt đầu</label>
                                    <input 
                                        type="datetime-local" 
                                        className="flex h-10 w-full rounded-md border border-zinc-300 px-3 py-2 text-sm focus:ring-emerald-500 focus:border-emerald-500" 
                                        value={form.startTime} 
                                        onChange={e => setForm({...form, startTime: e.target.value})} 
                                        required 
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium mb-1.5 text-zinc-700">Thời gian kết thúc</label>
                                    <input 
                                        type="datetime-local" 
                                        className="flex h-10 w-full rounded-md border border-zinc-300 px-3 py-2 text-sm focus:ring-emerald-500 focus:border-emerald-500" 
                                        value={form.endTime} 
                                        onChange={e => setForm({...form, endTime: e.target.value})} 
                                        required 
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="pt-4 border-t border-zinc-100 flex justify-end gap-3">
                            <Button type="button" variant="ghost" onClick={() => setView('list')}>Hủy bỏ</Button>
                            <Button 
                                type="submit" 
                                className="bg-emerald-600 hover:bg-emerald-700 text-white min-w-[150px]"
                                disabled={loading}
                            >
                                {loading ? "Đang tạo..." : "Tạo chiến dịch"}
                            </Button>
                        </div>
                    </form>
                </Card>
            </div>
        );
    }

    // LIST VIEW
    return (
        <div className="space-y-6">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold text-zinc-900">Quản lý Flash Sale</h1>
                    <p className="text-zinc-500">Theo dõi và quản lý các chiến dịch khuyến mãi.</p>
                </div>
                <Button onClick={() => setView('create')} className="bg-emerald-600 hover:bg-emerald-700 text-white shadow-sm">
                    <Plus size={20} className="mr-2" /> Tạo chiến dịch mới
                </Button>
            </div>

            <Card className="overflow-hidden border-zinc-200 shadow-sm bg-white">
                <div className="overflow-x-auto">
                    <table className="w-full text-left text-sm text-zinc-600">
                        <thead className="bg-zinc-50 text-xs uppercase font-semibold text-zinc-500 border-b border-zinc-200">
                            <tr>
                                <th className="px-6 py-4">Sản phẩm</th>
                                <th className="px-6 py-4">Trạng thái</th>
                                <th className="px-6 py-4">Tiến độ</th>
                                <th className="px-6 py-4">Giá</th>
                                <th className="px-6 py-4">Thời gian</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-zinc-100">
                            {sales.length === 0 && !loading && (
                                <tr>
                                    <td colSpan={5} className="px-6 py-12 text-center text-zinc-400">
                                        Chưa có chương trình nào. Hãy tạo mới để bắt đầu.
                                    </td>
                                </tr>
                            )}
                            {sales.map((sale) => {
                                const sold = sale.totalQuantity - sale.remainingQuantity;
                                const progress = (sold / sale.totalQuantity) * 100;
                                
                                return (
                                    <tr key={sale.id.value} className="hover:bg-zinc-50/50 transition-colors">
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-3">
                                                <div className="h-10 w-10 rounded bg-zinc-100 flex items-center justify-center text-zinc-400">
                                                    <Package size={20} />
                                                </div>
                                                <div className="max-w-[200px]">
                                                    <div className="font-medium text-zinc-900 truncate" title={sale.productId}>
                                                        {sale.productId}
                                                    </div>
                                                    <div className="text-xs text-zinc-400">ID: {sale.id.value.substring(0, 8)}...</div>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <Badge className={`${getStatusColor(sale.status)} shadow-none`}>
                                                {sale.status}
                                            </Badge>
                                        </td>
                                        <td className="px-6 py-4 min-w-[150px]">
                                            <div className="flex justify-between text-xs mb-1.5">
                                                <span className="font-medium text-zinc-700">{sold} đã bán</span>
                                                <span className="text-zinc-400">trên {sale.totalQuantity}</span>
                                            </div>
                                            <div className="h-2 w-full bg-zinc-100 rounded-full overflow-hidden">
                                                <div 
                                                    className={`h-full rounded-full ${progress >= 100 ? 'bg-red-500' : 'bg-emerald-500'}`} 
                                                    style={{ width: `${progress}%` }} 
                                                />
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="font-bold text-zinc-900">
                                                {formatCurrency(sale.price.amount, sale.price.currency)}
                                            </div>
                                            <div className="text-xs text-zinc-400 line-through">
                                                {formatCurrency(sale.originalPrice.amount, sale.originalPrice.currency)}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="flex flex-col gap-1 text-xs text-zinc-500">
                                                <div className="flex items-center gap-1.5">
                                                    <Calendar size={12} />
                                                    <span>BĐ: {new Date(sale.startTime).toLocaleDateString()}</span>
                                                </div>
                                                <div className="flex items-center gap-1.5">
                                                    <Clock size={12} />
                                                    <span>KT: {new Date(sale.endTime).toLocaleDateString()}</span>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                </div>
            </Card>
        </div>
    );
}
