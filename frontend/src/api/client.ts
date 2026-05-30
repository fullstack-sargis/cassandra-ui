import type { ConnectionRequest, DataPage, Keyspace, Table, TableSchema } from './types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(options?.headers ?? {}),
    },
  });

  if (!response.ok) {
    const body = await response.json().catch(() => ({ message: response.statusText }));
    throw new Error(body.message ?? 'Request failed');
  }

  return response.json() as Promise<T>;
}

export const api = {
  testConnection(payload: ConnectionRequest) {
    return request<{ success: boolean; message: string }>('/connections/test', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
  keyspaces() {
    return request<Keyspace[]>('/keyspaces');
  },
  tables(keyspace: string) {
    return request<Table[]>(`/keyspaces/${encodeURIComponent(keyspace)}/tables`);
  },
  schema(keyspace: string, table: string) {
    return request<TableSchema>(`/keyspaces/${encodeURIComponent(keyspace)}/tables/${encodeURIComponent(table)}/schema`);
  },
  data(keyspace: string, table: string, page: number, size: number) {
    return request<DataPage>(`/keyspaces/${encodeURIComponent(keyspace)}/tables/${encodeURIComponent(table)}/data?page=${page}&size=${size}`);
  },
  query(query: string, pageSize: number) {
    return request<DataPage>('/query', {
      method: 'POST',
      body: JSON.stringify({ query, pageSize }),
    });
  },
};
