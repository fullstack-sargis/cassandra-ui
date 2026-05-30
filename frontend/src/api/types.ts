export type ConnectionRequest = {
  host: string;
  port: number;
  datacenter: string;
  username?: string;
  password?: string;
  keyspace?: string;
};

export type Keyspace = {
  name: string;
  system: boolean;
};

export type Table = {
  name: string;
};

export type Column = {
  name: string;
  type: string;
  kind: string;
};

export type TableSchema = {
  keyspace: string;
  table: string;
  columns: Column[];
  createStatement: string;
};

export type DataPage = {
  columns: string[];
  rows: Record<string, unknown>[];
  page: number;
  size: number;
  hasMore: boolean;
};
