import { useEffect, useState } from 'react';
import { AlertCircle, ChevronLeft, ChevronRight, RefreshCw } from 'lucide-react';
import { api } from '../api/client';
import type { ConnectionRequest, DataPage, Keyspace, Table, TableSchema } from '../api/types';
import { ConnectionForm } from '../components/ConnectionForm';
import { DataTable } from '../components/DataTable';
import { QueryEditor } from '../components/QueryEditor';
import { SchemaViewer } from '../components/SchemaViewer';
import { Sidebar } from '../components/Sidebar';

type Tab = 'schema' | 'data' | 'query';

export function Dashboard() {
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState('');
  const [connecting, setConnecting] = useState(false);
  const [keyspaces, setKeyspaces] = useState<Keyspace[]>([]);
  const [tables, setTables] = useState<Table[]>([]);
  const [selectedKeyspace, setSelectedKeyspace] = useState<string>();
  const [selectedTable, setSelectedTable] = useState<string>();
  const [schema, setSchema] = useState<TableSchema>();
  const [data, setData] = useState<DataPage>();
  const [queryData, setQueryData] = useState<DataPage>();
  const [loadingTables, setLoadingTables] = useState(false);
  const [loadingSchema, setLoadingSchema] = useState(false);
  const [loadingData, setLoadingData] = useState(false);
  const [runningQuery, setRunningQuery] = useState(false);
  const [tab, setTab] = useState<Tab>('schema');
  const [page, setPage] = useState(0);
  const pageSize = 50;

  const connect = async (connection: ConnectionRequest) => {
    setConnecting(true);
    setError('');
    try {
      await api.testConnection(connection);
      setConnected(true);
      const spaces = await api.keyspaces();
      setKeyspaces(spaces);
      setSelectedKeyspace(connection.keyspace || spaces.find(item => !item.system)?.name || spaces[0]?.name);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setConnecting(false);
    }
  };

  useEffect(() => {
    if (!selectedKeyspace) return;
    setLoadingTables(true);
    setSelectedTable(undefined);
    setSchema(undefined);
    setData(undefined);
    api.tables(selectedKeyspace)
      .then(setTables)
      .catch(err => setError(errorMessage(err)))
      .finally(() => setLoadingTables(false));
  }, [selectedKeyspace]);

  useEffect(() => {
    if (!selectedKeyspace || !selectedTable) return;
    setLoadingSchema(true);
    setPage(0);
    api.schema(selectedKeyspace, selectedTable)
      .then(setSchema)
      .catch(err => setError(errorMessage(err)))
      .finally(() => setLoadingSchema(false));
    loadData(0);
  }, [selectedKeyspace, selectedTable]);

  const loadData = async (nextPage = page) => {
    if (!selectedKeyspace || !selectedTable) return;
    setLoadingData(true);
    setError('');
    try {
      const pageData = await api.data(selectedKeyspace, selectedTable, nextPage, pageSize);
      setData(pageData);
      setPage(nextPage);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoadingData(false);
    }
  };

  const runQuery = async (query: string, queryPageSize: number) => {
    setRunningQuery(true);
    setError('');
    try {
      setQueryData(await api.query(query, queryPageSize));
      setTab('query');
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setRunningQuery(false);
    }
  };

  return (
    <main className="app-shell">
      <ConnectionForm loading={connecting} onConnect={connect} />

      {error && (
        <div className="alert">
          <AlertCircle size={18} />
          {error}
        </div>
      )}

      <div className="workspace">
        <Sidebar
          keyspaces={keyspaces}
          tables={tables}
          selectedKeyspace={selectedKeyspace}
          selectedTable={selectedTable}
          loadingTables={loadingTables}
          onSelectKeyspace={setSelectedKeyspace}
          onSelectTable={setSelectedTable}
        />

        <section className="content-panel">
          <div className="content-header">
            <div>
              <span className="eyebrow">{connected ? 'Connected' : 'Disconnected'}</span>
              <h2>{selectedTable ? `${selectedKeyspace}.${selectedTable}` : 'Browse Cassandra'}</h2>
            </div>
            <div className="tabs">
              <button className={tab === 'schema' ? 'active' : ''} onClick={() => setTab('schema')}>Schema</button>
              <button className={tab === 'data' ? 'active' : ''} onClick={() => setTab('data')}>Data</button>
              <button className={tab === 'query' ? 'active' : ''} onClick={() => setTab('query')}>Query</button>
            </div>
          </div>

          {tab === 'schema' && <SchemaViewer schema={schema} loading={loadingSchema} />}

          {tab === 'data' && (
            <>
              <div className="table-actions">
                <button className="icon-button" onClick={() => loadData(page)} disabled={!selectedTable || loadingData} title="Refresh">
                  <RefreshCw size={17} />
                </button>
                <button className="icon-button" onClick={() => loadData(Math.max(0, page - 1))} disabled={page === 0 || loadingData} title="Previous page">
                  <ChevronLeft size={17} />
                </button>
                <span className="page-indicator">Page {page + 1}</span>
                <button className="icon-button" onClick={() => loadData(page + 1)} disabled={!data?.hasMore || loadingData} title="Next page">
                  <ChevronRight size={17} />
                </button>
              </div>
              <DataTable data={data} loading={loadingData} />
            </>
          )}

          {tab === 'query' && (
            <>
              <QueryEditor loading={runningQuery} onRun={runQuery} />
              <DataTable data={queryData} loading={runningQuery} />
            </>
          )}
        </section>
      </div>
    </main>
  );
}

function errorMessage(error: unknown) {
  return error instanceof Error ? error.message : 'Something went wrong';
}
