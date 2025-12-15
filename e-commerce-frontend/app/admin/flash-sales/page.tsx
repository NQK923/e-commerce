"use client";

import { useState } from "react";
import { flashSaleApi } from "@/api/flashSaleApi";

export default function CreateFlashSalePage() {
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
        try {
            await flashSaleApi.create({
                ...form,
                startTime: new Date(form.startTime).toISOString(),
                endTime: new Date(form.endTime).toISOString()
            });
            alert("Flash Sale Created!");
        } catch (e) {
            alert("Error: " + e);
        }
    };

    return (
        <div className="container mx-auto p-4">
            <h1 className="text-2xl font-bold mb-4">Create Flash Sale</h1>
            <form onSubmit={handleSubmit} className="space-y-4 max-w-lg">
                <div>
                    <label className="block">Product ID</label>
                    <input className="border p-2 w-full" value={form.productId} onChange={e => setForm({...form, productId: e.target.value})} required />
                </div>
                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block">Price</label>
                        <input type="number" className="border p-2 w-full" value={form.price} onChange={e => setForm({...form, price: Number(e.target.value)})} required />
                    </div>
                    <div>
                        <label className="block">Currency</label>
                        <input className="border p-2 w-full" value={form.currency} onChange={e => setForm({...form, currency: e.target.value})} required />
                    </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block">Original Price</label>
                        <input type="number" className="border p-2 w-full" value={form.originalPrice} onChange={e => setForm({...form, originalPrice: Number(e.target.value)})} required />
                    </div>
                    <div>
                         <label className="block">Currency</label>
                         <input className="border p-2 w-full" value={form.originalCurrency} onChange={e => setForm({...form, originalCurrency: e.target.value})} required />
                    </div>
                </div>
                <div>
                    <label className="block">Total Quantity</label>
                    <input type="number" className="border p-2 w-full" value={form.totalQuantity} onChange={e => setForm({...form, totalQuantity: Number(e.target.value)})} required />
                </div>
                <div>
                    <label className="block">Start Time</label>
                    <input type="datetime-local" className="border p-2 w-full" value={form.startTime} onChange={e => setForm({...form, startTime: e.target.value})} required />
                </div>
                <div>
                    <label className="block">End Time</label>
                    <input type="datetime-local" className="border p-2 w-full" value={form.endTime} onChange={e => setForm({...form, endTime: e.target.value})} required />
                </div>
                <button type="submit" className="bg-blue-500 text-white px-4 py-2 rounded">Create</button>
            </form>
        </div>
    );
}
