export type PaginatedResponse<T> = {
  items: T[];
  page: number;
  size: number;
  total: number;
};

export type ApiListParams = {
  page?: number;
  size?: number;
  sort?: string;
};
