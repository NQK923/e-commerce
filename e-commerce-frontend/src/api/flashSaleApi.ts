import { apiRequest } from "../lib/api-client";

export interface FlashSale {
    id: { value: string };
    productId: string;
    price: { amount: number; currency: string };
    originalPrice: { amount: number; currency: string };
    startTime: string;
    endTime: string;
    totalQuantity: number;
    remainingQuantity: number;
    status: string;
}

export interface CreateFlashSaleRequest {
    productId: string;
    price: number;
    currency: string;
    originalPrice: number;
    originalCurrency: string;
    startTime: string;
    endTime: string;
    totalQuantity: number;
}

export const flashSaleApi = {
    create: (data: CreateFlashSaleRequest) => 
        apiRequest<{ value: string }>('/api/admin/flash-sales', { 
            method: 'POST', 
            body: data 
        }),

    listActive: () => 
        apiRequest<FlashSale[]>('/api/flash-sales')
};