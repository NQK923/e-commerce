'use client';

import { useEffect, useState } from "react";
import { productApi } from "../api/productApi";
import { Product, ProductListParams } from "../types/product";
import { PaginatedResponse } from "../types/common";

type State = {
  data: PaginatedResponse<Product> | null;
  loading: boolean;
  error?: string;
};

export const useProducts = (params: ProductListParams) => {
  const [state, setState] = useState<State>({ data: null, loading: true });
  const [version, setVersion] = useState(0);

  useEffect(() => {
    const load = async () => {
      setState((prev) => ({ ...prev, loading: true, error: undefined }));
      try {
        const response = await productApi.list(params);
        setState({ data: response, loading: false });
      } catch (error) {
        const message = error instanceof Error ? error.message : "Failed to load products";
        setState({ data: null, loading: false, error: message });
      }
    };
    void load();
  }, [params, version]);

  return { ...state, reload: () => setVersion((v) => v + 1) };
};
