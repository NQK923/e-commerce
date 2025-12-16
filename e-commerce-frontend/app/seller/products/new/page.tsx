'use client';

import React, { Suspense, useState, useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useForm, FormProvider } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { productApi } from "@/src/api/productApi";
import { Button } from "@/src/components/ui/button";
import { Spinner } from "@/src/components/ui/spinner";
import { useToast } from "@/src/components/ui/toast-provider";
import { useRequireAuth } from "@/src/hooks/use-require-auth";
import { uploadToBucket } from "@/src/lib/storage";
import { config } from "@/src/config/env";
import { DEFAULT_PRODUCT_CATEGORY } from "@/src/constants/categories";
import { productSchema, ProductFormValues } from "@/src/schemas/product";
import { ProductBasicInfo } from "@/src/components/seller/products/ProductBasicInfo";
import { ProductMedia } from "@/src/components/seller/products/ProductMedia";
import { ProductPricing } from "@/src/components/seller/products/ProductPricing";
import { ProductVariants } from "@/src/components/seller/products/ProductVariants";

type ImageItem = {
    id: string;
    url: string;
    file?: File;
};

function NewProductContent() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const editingProductId = searchParams.get("productId");
    const isEdit = Boolean(editingProductId);
    const { addToast } = useToast();
    const { user, initializing } = useRequireAuth("/login");

    const [loading, setLoading] = useState(false);
    const [prefilling, setPrefilling] = useState(false);
    
    // Unified Image State
    const [images, setImages] = useState<ImageItem[]>([]);

    const methods = useForm({
        resolver: zodResolver(productSchema),
        defaultValues: {
            name: "",
            description: "",
            price: "",
            quantity: "",
            currency: "VND",
            categoryId: DEFAULT_PRODUCT_CATEGORY,
            hasVariants: false,
            variants: [],
            images: [],
        },
    });

    // Cleanup object URLs when images are removed or component unmounts
    useEffect(() => {
        return () => {
            images.forEach(img => {
                if (img.file) URL.revokeObjectURL(img.url);
            });
        };
    }, [images]);

    // Sync form "images" field with state for validation
    useEffect(() => {
        const imageUrls = images.map(img => img.url);
        methods.setValue("images", imageUrls);
        if (methods.formState.isSubmitted) {
            methods.trigger("images");
        }
    }, [images, methods]);

    useEffect(() => {
        if (initializing) return;
        if (user && !user.roles?.includes("SELLER")) {
            router.replace("/seller/register?next=/seller/products/new");
        }
    }, [initializing, router, user]);

    useEffect(() => {
        if (!editingProductId) return;
        const load = async () => {
            setPrefilling(true);
            try {
                const detail = await productApi.detail(editingProductId);
                
                // Prepare variants
                const hasVariants = !!(detail.variants && detail.variants.length > 0);
                const variants = hasVariants ? detail.variants!.map(v => ({
                    sku: v.sku,
                    name: v.name,
                    price: v.price.toString(),
                    quantity: v.quantity ?? 0
                })) : [];

                // Prepare Images
                const initialImages: ImageItem[] = detail.images?.map(img => ({
                    id: img.id || img.url,
                    url: img.url
                })) ?? [];
                setImages(initialImages);

                methods.reset({
                    name: detail.name ?? "",
                    description: detail.description ?? "",
                    price: detail.price?.toString() ?? "",
                    quantity: detail.stock?.toString() ?? "",
                    currency: detail.currency ?? "VND",
                    categoryId: detail.category ?? DEFAULT_PRODUCT_CATEGORY,
                    hasVariants: hasVariants,
                    variants: variants,
                    images: initialImages.map(i => i.url)
                });

            } catch (error) {
                console.error("Failed to load product for editing", error);
                addToast("Không tải được sản phẩm để chỉnh sửa", "error");
                router.back();
            } finally {
                setPrefilling(false);
            }
        };
        void load();
    }, [addToast, editingProductId, router, methods]);

    // Image Handlers
    const handleSetCover = (file: File) => {
        const newItem: ImageItem = {
            id: Math.random().toString(36),
            url: URL.createObjectURL(file),
            file
        };
        setImages(prev => {
            const newImages = [...prev];
            if (newImages.length > 0) {
                // If replacing existing cover, cleanup if it was a blob
                if (newImages[0].file) URL.revokeObjectURL(newImages[0].url);
                newImages[0] = newItem;
            } else {
                newImages.push(newItem);
            }
            return newImages;
        });
    };

    const handleAddGallery = (files: File[]) => {
        const newItems: ImageItem[] = files.map(file => ({
            id: Math.random().toString(36),
            url: URL.createObjectURL(file),
            file
        }));
        setImages(prev => [...prev, ...newItems]);
    };

    const handleReplaceImage = (index: number, file: File) => {
        const newItem: ImageItem = {
            id: Math.random().toString(36),
            url: URL.createObjectURL(file),
            file
        };
        setImages(prev => {
            const newImages = [...prev];
            if (newImages[index]) {
                if (newImages[index].file) URL.revokeObjectURL(newImages[index].url);
                newImages[index] = newItem;
            }
            return newImages;
        });
    };

    const handleRemoveImage = (index: number) => {
        setImages(prev => {
            const target = prev[index];
            if (target && target.file) URL.revokeObjectURL(target.url);
            return prev.filter((_, i) => i !== index);
        });
    };

    const onSubmit = async (data: ProductFormValues) => {
        if (images.length === 0) {
             methods.setError("images", { message: "Vui lòng tải lên ít nhất 1 hình ảnh (Ảnh bìa)" });
             return;
        }

        setLoading(true);
        try {
            // Upload images that are new Files
            const finalImagesPayload: { id?: string; url: string; primary: boolean; sortOrder: number }[] = [];
            
            if (images.some(img => img.file)) {
                addToast("Đang tải lên hình ảnh...", "info");
            }

            for (let i = 0; i < images.length; i++) {
                const img = images[i];
                let url = img.url;
                
                if (img.file) {
                    url = await uploadToBucket(config.supabaseProductBucket, img.file);
                }
                
                finalImagesPayload.push({
                    id: img.file ? undefined : img.id, // Only send ID for existing images (no file)
                    url,
                    primary: i === 0,
                    sortOrder: i
                });
            }

            // Prepare Payload
            const payload = {
                name: data.name,
                description: data.description,
                price: parseFloat(data.price),
                currency: data.currency || "VND",
                categoryId: data.categoryId,
                sellerId: user.id,
                quantity: data.hasVariants ? 
                    data.variants!.reduce((sum, v) => sum + v.quantity, 0) 
                    : (parseInt(data.quantity || "0")),
                images: finalImagesPayload,
                variants: data.hasVariants ? data.variants!.map(v => ({
                    ...v,
                    price: parseFloat(v.price)
                })) : undefined
            };

            if (isEdit && editingProductId) {
                await productApi.update(editingProductId, payload);
                addToast("Đã cập nhật sản phẩm", "success");
            } else {
                addToast("Đang tạo sản phẩm...", "info");
                await productApi.create(payload);
                addToast("Đã tạo sản phẩm thành công", "success");
            }

            router.replace("/seller/dashboard");

        } catch (error) {
            console.error(error);
            const message = error instanceof Error ? error.message : "Có lỗi xảy ra";
            addToast(message, "error");
        } finally {
            setLoading(false);
        }
    };

    if (initializing || prefilling || !user) {
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
                <h1 className="text-3xl font-bold text-zinc-900">{isEdit ? "Chỉnh sửa sản phẩm" : "Thêm sản phẩm mới"}</h1>
                <p className="text-sm text-zinc-600">Điền đầy đủ thông tin để sản phẩm của bạn nổi bật hơn.</p>
            </div>

            <FormProvider {...methods}>
                <form className="space-y-8" onSubmit={methods.handleSubmit(onSubmit)}>
                    <ProductBasicInfo />
                    
                    <ProductMedia 
                        images={images.map(i => i.url)}
                        onSetCover={handleSetCover}
                        onAddGallery={handleAddGallery}
                        onRemove={handleRemoveImage}
                        onReplace={handleReplaceImage}
                        error={methods.formState.errors.images?.message}
                        uploading={loading}
                    />
                    
                    <ProductPricing />
                    
                    <ProductVariants />

                    <div className="flex items-center gap-4 pt-4">
                        <Button 
                            type="submit" 
                            disabled={loading}
                            className="bg-emerald-600 hover:bg-emerald-700 px-8 h-12 text-base"
                        >
                            {loading ? "Đang xử lý..." : "Lưu & Hiển thị"}
                        </Button>
                        <Button 
                            type="button" 
                            variant="ghost" 
                            onClick={() => router.back()} 
                            className="h-12 px-6"
                        >
                            Hủy bỏ
                        </Button>
                    </div>
                </form>
            </FormProvider>
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
