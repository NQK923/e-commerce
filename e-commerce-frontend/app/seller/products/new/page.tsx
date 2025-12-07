'use client';

import React, { Suspense, useState } from "react";
import { useRouter } from "next/navigation";
import Image from "next/image";
import { productApi } from "@/src/api/productApi";
import { Button } from "@/src/components/ui/button";
import { Input } from "@/src/components/ui/input";
import { Spinner } from "@/src/components/ui/spinner";
import { useToast } from "@/src/components/ui/toast-provider";
import { useRequireAuth } from "@/src/hooks/use-require-auth";
import { uploadToBucket } from "@/src/lib/storage";
import { ProductVariantRequest } from "@/src/types/product";
import { config } from "@/src/config/env";

const CATEGORIES = [
  "Điện tử",
  "Thời trang",
  "Gia dụng",
  "Sách",
  "Làm đẹp",
  "Sức khỏe",
  "Đồ chơi",
  "Thể thao",
  "Ô tô - Xe máy",
  "Bách hóa online",
  "Khác",
];

function NewProductContent() {
    const router = useRouter();
    const {addToast} = useToast();
    const {user, initializing} = useRequireAuth("/login");

    const [form, setForm] = useState({
        name: "",
        description: "",
        price: "",
        quantity: "",
        currency: "VND",
        categoryId: CATEGORIES[0],
    });

    const [selectedImages, setSelectedImages] = useState<File[]>([]);
    const [previewImages, setPreviewImages] = useState<string[]>([]);
    const [hasVariants, setHasVariants] = useState(false);
    const [variants, setVariants] = useState<ProductVariantRequest[]>([]);
    const [newVariant, setNewVariant] = useState({sku: "", name: "", price: "", quantity: ""});

    const [loading, setLoading] = useState(false);
    const [uploading, setUploading] = useState(false);

    React.useEffect(() => {
        if (initializing) return;
        if (user && !user.roles?.includes("SELLER")) {
            router.replace("/seller/register?next=/seller/products/new");
        }
    }, [initializing, router, user]);

    const handleAddVariant = () => {
        if (!newVariant.sku || !newVariant.name || !newVariant.price || !newVariant.quantity) {
            addToast("Vui lòng nhập đầy đủ thông tin phân loại", "error");
            return;
        }
        setVariants([...variants, {
            ...newVariant,
            price: parseFloat(newVariant.price),
            quantity: parseInt(newVariant.quantity)
        }]);
        setNewVariant({sku: "", name: "", price: "", quantity: ""});
    };

    const removeVariant = (index: number) => {
        setVariants(variants.filter((_, i) => i !== index));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!form.name || !form.price || selectedImages.length === 0) {
            addToast("Vui lòng nhập tên, giá và hình ảnh sản phẩm", "error");
            return;
        }
        if (hasVariants && variants.length === 0) {
            addToast("Vui lòng thêm ít nhất một phân loại hoặc tắt chế độ phân loại", "error");
            return;
        }

        setLoading(true);
        setUploading(true);
        try {
            addToast("Đang tạo sản phẩm...", "info");
            const createdProduct = await productApi.create({
                name: form.name,
                description: form.description,
                price: parseFloat(form.price),
                currency: form.currency || "VND",
                quantity: form.quantity ? parseInt(form.quantity) : 0,
                categoryId: form.categoryId || undefined,
                images: [],
                variants: hasVariants ? variants : undefined,
            });

            if (!createdProduct || !createdProduct.id) {
                throw new Error("Không nhận được ID sản phẩm sau khi tạo");
            }

            addToast("Đang tải lên hình ảnh...", "info");
            const uploaded: string[] = [];
            for (const file of selectedImages) {
                const url = await uploadToBucket(config.supabaseProductBucket, file);
                uploaded.push(url);
            }

            const [primaryImage, ...galleryImages] = uploaded;

            addToast("Đang cập nhật hình ảnh sản phẩm...", "info");
            await productApi.update(createdProduct.id, {
                name: form.name,
                description: form.description,
                price: parseFloat(form.price),
                currency: form.currency || "VND",
                quantity: form.quantity ? parseInt(form.quantity) : 0,
                categoryId: form.categoryId || undefined,
                images: [
                    {url: primaryImage, primary: true},
                    ...galleryImages.map((url, idx) => ({url, primary: false, sortOrder: idx + 1})),
                ],
                variants: hasVariants ? variants : undefined,
            });

            addToast("Đã tạo sản phẩm thành công", "success");
            router.replace("/seller/dashboard");
        } catch (error) {
            console.error(error);
            const message = error instanceof Error ? error.message : "Có lỗi xảy ra";
            addToast(message, "error");
        } finally {
            setLoading(false);
            setUploading(false);
        }
    };

    if (initializing || !user) {
            return (
                <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
                    <Spinner/>
                    Đang tải...
                </div>
            );
        }

        if (!user.roles?.includes("SELLER")) {
            return (
                <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
                    <Spinner/>
                    Đang chuyển đến trang dành cho kênh người bán...
                </div>
            );
        }

        return (
            <div className="mx-auto max-w-4xl px-4 py-10 space-y-8">
                <div>
                    <p className="text-sm font-semibold text-emerald-700">Kênh người bán</p>
                    <h1 className="text-3xl font-bold text-zinc-900">Thêm sản phẩm mới</h1>
                    <p className="text-sm text-zinc-600">Điền đầy đủ thông tin để sản phẩm của bạn nổi bật hơn.</p>
                </div>

                <form className="space-y-8" onSubmit={handleSubmit}>
                    {/* Basic Info */}
                    <div className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm space-y-6">
                        <h2 className="text-lg font-bold text-zinc-900">Thông tin cơ bản</h2>
                        <div className="grid gap-6">
                            <div className="grid gap-2">
                                <label className="text-sm font-medium text-zinc-700">Tên sản phẩm <span
                                    className="text-red-500">*</span></label>
                                <Input
                                    value={form.name}
                                    onChange={(e) => setForm((prev) => ({...prev, name: e.target.value}))}
                                    placeholder="VD: Áo thun nam cotton..."
                                    required
                                />
                            </div>

                            <div className="grid gap-2">
                                <label className="text-sm font-medium text-zinc-700">Ngành hàng <span
                                    className="text-red-500">*</span></label>
                                <select
                                    className="h-10 w-full rounded-lg border border-zinc-200 bg-white px-3 text-sm outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
                                    value={form.categoryId}
                                    onChange={(e) => setForm((prev) => ({...prev, categoryId: e.target.value}))}
                                >
                                    {CATEGORIES.map(cat => (
                                        <option key={cat} value={cat}>{cat}</option>
                                    ))}
                                </select>
                            </div>

                            <div className="grid gap-2">
                                <label className="text-sm font-medium text-zinc-700">Mô tả sản phẩm</label>
                                <textarea
                                    className="min-h-[150px] rounded-lg border border-zinc-200 bg-white px-3 py-2 text-sm text-zinc-800 outline-none transition focus:border-emerald-400 focus:ring-2 focus:ring-emerald-100"
                                    value={form.description}
                                    onChange={(e) => setForm((prev) => ({...prev, description: e.target.value}))}
                                    placeholder="Mô tả chi tiết về sản phẩm..."
                                />
                            </div>
                        </div>
                    </div>

                    {/* Media */}
                    <div className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm space-y-6">
                        <h2 className="text-lg font-bold text-zinc-900">Hình ảnh sản phẩm</h2>
                        <div className="space-y-4">
                            <div className="flex flex-col gap-2 text-sm">
                                <label className="font-medium text-zinc-700">Ảnh bìa & Thư viện ảnh <span
                                    className="text-red-500">*</span></label>
                                <p className="text-xs text-zinc-500">Ảnh đầu tiên sẽ là ảnh bìa. Tải lên nhiều ảnh để
                                    khách hàng dễ hình dung.</p>
                                <div className="mt-2 flex flex-wrap gap-4">
                                    <label
                                        className="flex h-24 w-24 cursor-pointer flex-col items-center justify-center rounded-lg border border-dashed border-zinc-300 bg-zinc-50 hover:bg-zinc-100">
                                        <span
                                            className="text-xs text-zinc-500 text-center px-1">{uploading ? "..." : "+ Thêm ảnh"}</span>
                                        <input
                                            type="file"
                                            className="hidden"
                                            accept="image/*"
                                            multiple
                                            disabled={uploading}
                                            onChange={(e) => {

                                                const files = Array.from(e.target.files ?? []);

                                                if (!files.length) return;

                                                const previews = files.map((file) => URL.createObjectURL(file));

                                                setSelectedImages((prev) => [...prev, ...files]);

                                                setPreviewImages((prev) => [...prev, ...previews]);

                                                addToast("Đã chọn ảnh, nhấn Lưu để tải lên", "success");
                                            }}
                                        />
                                    </label>
                                    {previewImages.map((url, idx) => (
                                        <div key={idx}
                                             className="relative h-24 w-24 overflow-hidden rounded-lg border border-zinc-200">
                                            <Image src={url} alt="preview" fill className="object-cover"/>
                                            {idx === 0 && <span
                                                className="absolute bottom-0 left-0 right-0 bg-black/60 text-center text-[10px] text-white">Ảnh bìa</span>}
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Pricing & Variants */}
                    <div className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm space-y-6">
                        <h2 className="text-lg font-bold text-zinc-900">Thông tin bán hàng</h2>

                        <div className="grid gap-4 sm:grid-cols-3">
                            <Input
                                label="Giá cơ bản (VND)"
                                type="number"
                                min="0"
                                required
                                value={form.price}
                                onChange={(e) => setForm((prev) => ({...prev, price: e.target.value}))}
                                placeholder="0"
                            />
                            <Input
                                label="Số lượng kho"
                                type="number"
                                min="0"
                                required={!hasVariants}
                                disabled={hasVariants}
                                value={form.quantity}
                                onChange={(e) => setForm((prev) => ({...prev, quantity: e.target.value}))}
                                placeholder="0"
                            />
                            <Input
                                label="Đơn vị tiền tệ"
                                value={form.currency}
                                onChange={(e) => setForm((prev) => ({...prev, currency: e.target.value}))}
                                disabled
                            />
                        </div>

                        <div className="border-t border-zinc-100 pt-4">
                            <label className="flex items-center gap-2 cursor-pointer select-none mb-4">
                                <input
                                    type="checkbox"
                                    className="h-4 w-4 rounded border-zinc-300 text-emerald-600 focus:ring-emerald-500"
                                    checked={hasVariants}
                                    onChange={(e) => setHasVariants(e.target.checked)}
                                />
                                <span className="font-medium text-zinc-900">Sản phẩm có nhiều phân loại (Màu sắc, Kích cỡ...)</span>
                            </label>

                            {hasVariants && (
                                <div className="rounded-lg border border-zinc-100 bg-zinc-50 p-4 space-y-4">
                                    <div className="grid grid-cols-1 gap-4 md:grid-cols-5 items-end">
                                        <Input
                                            label="Tên phân loại (VD: Đỏ, Size L)"
                                            value={newVariant.name}
                                            onChange={e => setNewVariant({...newVariant, name: e.target.value})}
                                        />
                                        <Input
                                            label="SKU (Mã kho)"
                                            value={newVariant.sku}
                                            onChange={e => setNewVariant({...newVariant, sku: e.target.value})}
                                        />
                                        <Input
                                            label="Giá bán"
                                            type="number"
                                            value={newVariant.price}
                                            onChange={e => setNewVariant({...newVariant, price: e.target.value})}
                                        />
                                        <Input
                                            label="Số lượng"
                                            type="number"
                                            value={newVariant.quantity}
                                            onChange={e => setNewVariant({...newVariant, quantity: e.target.value})}
                                        />
                                        <Button type="button" onClick={handleAddVariant}
                                                className="bg-white border border-zinc-300 text-zinc-700 hover:bg-zinc-50">Thêm</Button>
                                    </div>

                                    {variants.length > 0 && (
                                        <div className="overflow-hidden rounded-lg border border-zinc-200 bg-white">
                                            <table className="w-full text-sm text-left">
                                                <thead className="bg-zinc-50 text-zinc-700">
                                                <tr>
                                                    <th className="px-4 py-3 font-medium">Tên phân loại</th>
                                                    <th className="px-4 py-3 font-medium">SKU</th>
                                                    <th className="px-4 py-3 font-medium">Giá</th>
                                                    <th className="px-4 py-3 font-medium">Số lượng</th>
                                                    <th className="px-4 py-3 font-medium">Hành động</th>
                                                </tr>
                                                </thead>
                                                <tbody className="divide-y divide-zinc-100">
                                                {variants.map((v, idx) => (
                                                    <tr key={idx}>
                                                        <td className="px-4 py-3">{v.name}</td>
                                                        <td className="px-4 py-3 text-zinc-500">{v.sku}</td>
                                                        <td className="px-4 py-3 font-medium text-emerald-600">{v.price.toLocaleString()}</td>
                                                        <td className="px-4 py-3 text-zinc-600">{v.quantity}</td>
                                                        <td className="px-4 py-3">
                                                            <button type="button" onClick={() => removeVariant(idx)}
                                                                    className="text-red-600 hover:underline text-xs">Xóa
                                                            </button>
                                                        </td>
                                                    </tr>
                                                ))}
                                                </tbody>
                                            </table>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                    </div>

                    <div className="flex items-center gap-4 pt-4">
                        <Button type="submit" disabled={loading}
                                className="bg-emerald-600 hover:bg-emerald-700 px-8 h-12 text-base">
                            {loading ? "Đang xử lý..." : "Lưu & Hiển thị"}
                        </Button>
                        <Button type="button" variant="ghost" onClick={() => router.back()} className="h-12 px-6">
                            Hủy bỏ
                        </Button>
                    </div>
                </form>
            </div>
        );
    }

    export default function NewProductPage() {
        return (
            <Suspense fallback={
                <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
                    <Spinner/>
                    Đang tải...
                </div>
            }>
                <NewProductContent/>
        </Suspense>
    );
}
