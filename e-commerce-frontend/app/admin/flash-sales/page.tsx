"use client";

import { useState } from "react";
import { flashSaleApi } from "../src/api/flashSaleApi";
import { Button } from "../src/components/ui/button";
import { Card } from "../src/components/ui/card";
import { useRouter } from "next/navigation";

export default function CreateFlashSalePage() {
    const router = useRouter();
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState<{ type: 'success' | 'error', text: string } | null>(null);
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

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setMessage(null);
        
        try {
            await flashSaleApi.create({
                ...form,
                startTime: new Date(form.startTime).toISOString(),
                endTime: new Date(form.endTime).toISOString()
            });
            setMessage({ type: 'success', text: "Flash Sale Created Successfully!" });
            // Reset form or redirect?
            // setForm({ ...initialState });
            setTimeout(() => router.push('/flash-sales'), 1500);
        } catch (e: any) {
            setMessage({ type: 'error', text: "Error creating Flash Sale: " + (e.message || e) });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container mx-auto p-4 max-w-2xl">
            <h1 className="text-2xl font-bold mb-6">Create New Flash Sale</h1>
            
            {message && (
                <div className={`p-4 mb-6 rounded-lg ${message.type === 'success' ? 'bg-emerald-50 text-emerald-700' : 'bg-red-50 text-red-700'}`}>
                    {message.text}
                </div>
            )}

            <Card className="p-6">
                <form onSubmit={handleSubmit} className="space-y-6">
                    <div>
                        <label className="block text-sm font-medium mb-1">Product ID</label>
                        <input 
                            className="flex h-10 w-full rounded-md border border-zinc-200 bg-white px-3 py-2 text-sm placeholder:text-zinc-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent" 
                            value={form.productId} 
                            onChange={e => setForm({...form, productId: e.target.value})} 
                            required 
                            placeholder="e.g. product-uuid"
                        />
                    </div>
                    
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm font-medium mb-1">Sale Price</label>
                            <input 
                                type="number" 
                                className="flex h-10 w-full rounded-md border border-zinc-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500" 
                                value={form.price} 
                                onChange={e => setForm({...form, price: Number(e.target.value)})} 
                                required 
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium mb-1">Currency</label>
                            <input 
                                className="flex h-10 w-full rounded-md border border-zinc-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500" 
                                value={form.currency} 
                                onChange={e => setForm({...form, currency: e.target.value})} 
                                required 
                            />
                        </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm font-medium mb-1">Original Price</label>
                            <input 
                                type="number" 
                                className="flex h-10 w-full rounded-md border border-zinc-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500" 
                                value={form.originalPrice} 
                                onChange={e => setForm({...form, originalPrice: Number(e.target.value)})} 
                                required 
                            />
                        </div>
                        <div>
                             <label className="block text-sm font-medium mb-1">Currency</label>
                             <input 
                                className="flex h-10 w-full rounded-md border border-zinc-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500" 
                                value={form.originalCurrency} 
                                onChange={e => setForm({...form, originalCurrency: e.target.value})} 
                                required 
                            />
                        </div>
                    </div>

                    <div>
                        <label className="block text-sm font-medium mb-1">Total Quantity</label>
                        <input 
                            type="number" 
                            className="flex h-10 w-full rounded-md border border-zinc-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500" 
                            value={form.totalQuantity} 
                            onChange={e => setForm({...form, totalQuantity: Number(e.target.value)})} 
                            required 
                        />
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm font-medium mb-1">Start Time</label>
                            <input 
                                type="datetime-local" 
                                className="flex h-10 w-full rounded-md border border-zinc-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500" 
                                value={form.startTime} 
                                onChange={e => setForm({...form, startTime: e.target.value})} 
                                required 
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium mb-1">End Time</label>
                            <input 
                                type="datetime-local" 
                                className="flex h-10 w-full rounded-md border border-zinc-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500" 
                                value={form.endTime} 
                                onChange={e => setForm({...form, endTime: e.target.value})} 
                                required 
                            />
                        </div>
                    </div>

                    <Button 
                        type="submit" 
                        className="w-full bg-emerald-600 hover:bg-emerald-700 text-white"
                        disabled={loading}
                    >
                        {loading ? "Creating..." : "Create Flash Sale"}
                    </Button>
                </form>
            </Card>
        </div>
    );
}