import { useEffect, useRef, useState } from 'react';
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
  const [notice, setNotice] = useState('');
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
  const [savingKeyspace, setSavingKeyspace] = useState(false);
  const [tab, setTab] = useState<Tab>('schema');
  const [page, setPage] = useState(0);
  const tablesRequestId = useRef(0);
  const tableDataRequestId = useRef(0);
  const pageSize = 50;

  const connect = async (connection: ConnectionRequest) => {
    setConnecting(true);
    setError('');
    setNotice('');
    try {
      await api.testConnection(connection);
      setConnected(true);
      const spaces = await api.keyspaces();
      setKeyspaces(spaces);
      selectKeyspace(connection.keyspace || spaces.find(item => !item.system)?.name || spaces[0]?.name);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setConnecting(false);
    }
  };

  const selectKeyspace = (keyspace?: string) => {
    tablesRequestId.current++;
    tableDataRequestId.current++;
    setSelectedKeyspace(keyspace);
    setSelectedTable(undefined);
    setSchema(undefined);
    setData(undefined);
    setTables([]);
    setPage(0);
    setLoadingSchema(false);
    setLoadingData(false);
  };

  const refreshKeyspaces = async (preferredKeyspace?: string, excludedKeyspace?: string) => {
    const spaces = await api.keyspaces();
    const selectableSpaces = excludedKeyspace
      ? spaces.filter(item => item.name !== excludedKeyspace)
      : spaces;
    setKeyspaces(selectableSpaces);
    const nextKeyspace =
      preferredKeyspace && selectableSpaces.some(item => item.name === preferredKeyspace)
        ? preferredKeyspace
        : selectableSpaces.find(item => !item.system)?.name || selectableSpaces[0]?.name;
    selectKeyspace(nextKeyspace);
    return spaces;
  };

  useEffect(() => {
    const requestId = ++tablesRequestId.current;
    if (!selectedKeyspace) {
      setTables([]);
      return;
    }
    setLoadingTables(true);
    api.tables(selectedKeyspace)
      .then(nextTables => {
        if (requestId === tablesRequestId.current) {
          setTables(nextTables);
        }
      })
      .catch(err => {
        if (requestId === tablesRequestId.current) {
          setError(errorMessage(err));
        }
      })
      .finally(() => {
        if (requestId === tablesRequestId.current) {
          setLoadingTables(false);
        }
      });
  }, [selectedKeyspace]);

  useEffect(() => {
    if (!selectedKeyspace || !selectedTable) return;
    const requestId = ++tableDataRequestId.current;
    setLoadingSchema(true);
    setPage(0);
    api.schema(selectedKeyspace, selectedTable)
      .then(nextSchema => {
        if (requestId === tableDataRequestId.current) {
          setSchema(nextSchema);
        }
      })
      .catch(err => {
        if (requestId === tableDataRequestId.current) {
          setError(errorMessage(err));
        }
      })
      .finally(() => {
        if (requestId === tableDataRequestId.current) {
          setLoadingSchema(false);
        }
      });
    loadData(selectedKeyspace, selectedTable, 0, requestId);
  }, [selectedKeyspace, selectedTable]);

  const loadData = async (
    keyspace = selectedKeyspace,
    table = selectedTable,
    nextPage = page,
    requestId = ++tableDataRequestId.current,
  ) => {
    if (!keyspace || !table) return;
    setLoadingData(true);
    setError('');
    try {
      const pageData = await api.data(keyspace, table, nextPage, pageSize);
      if (requestId === tableDataRequestId.current) {
        setData(pageData);
        setPage(nextPage);
      }
    } catch (err) {
      if (requestId === tableDataRequestId.current) {
        setError(errorMessage(err));
      }
    } finally {
      if (requestId === tableDataRequestId.current) {
        setLoadingData(false);
      }
    }
  };

  const createKeyspace = async (name: string, replicationFactor: number, durableWrites: boolean) => {
    setSavingKeyspace(true);
    setError('');
    setNotice('');
    try {
      const response = await api.createKeyspace({ name, replicationFactor, durableWrites });
      await refreshKeyspaces(name);
      setNotice(response.message);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setSavingKeyspace(false);
    }
  };

  const dropKeyspace = async (keyspace: string) => {
    if (!window.confirm(`Drop keyspace "${keyspace}"? This deletes its tables and data.`)) return;
    setSavingKeyspace(true);
    setError('');
    setNotice('');
    try {
      const response = await api.dropKeyspace(keyspace);
      setKeyspaces(current => current.filter(item => item.name !== keyspace));
      await refreshKeyspaces(undefined, keyspace);
      setNotice(response.message);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setSavingKeyspace(false);
    }
  };

  const runQuery = async (query: string) => {
    setRunningQuery(true);
    setError('');
    setNotice('');
    try {
      const response = await api.query(query);
      setQueryData(response);
      if (response.message) {
        setNotice(response.message);
      }
      const spaces = await api.keyspaces();
      setKeyspaces(spaces);
      const keyspaceExists = selectedKeyspace && spaces.some(item => item.name === selectedKeyspace);
      if (keyspaceExists) {
        const nextTables = await api.tables(selectedKeyspace);
        setTables(nextTables);
        if (selectedTable && nextTables.some(item => item.name === selectedTable)) {
          await loadData(selectedKeyspace, selectedTable, 0);
          setSchema(await api.schema(selectedKeyspace, selectedTable));
        } else if (selectedTable) {
          setSelectedTable(undefined);
          setSchema(undefined);
          setData(undefined);
        }
      } else if (selectedKeyspace) {
        selectKeyspace(spaces.find(item => !item.system)?.name || spaces[0]?.name);
      }
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

      {notice && <div className="notice">{notice}</div>}

      <div className="workspace">
        <Sidebar
          keyspaces={keyspaces}
          tables={tables}
          selectedKeyspace={selectedKeyspace}
          selectedTable={selectedTable}
          loadingTables={loadingTables}
          savingKeyspace={savingKeyspace}
          onSelectKeyspace={selectKeyspace}
          onSelectTable={setSelectedTable}
          onCreateKeyspace={createKeyspace}
          onDropKeyspace={dropKeyspace}
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
                <button className="icon-button" onClick={() => loadData(selectedKeyspace, selectedTable, page)} disabled={!selectedTable || loadingData} title="Refresh">
                  <RefreshCw size={17} />
                </button>
                <button className="icon-button" onClick={() => loadData(selectedKeyspace, selectedTable, Math.max(0, page - 1))} disabled={page === 0 || loadingData} title="Previous page">
                  <ChevronLeft size={17} />
                </button>
                <span className="page-indicator">Page {page + 1}</span>
                <button className="icon-button" onClick={() => loadData(selectedKeyspace, selectedTable, page + 1)} disabled={!data?.hasMore || loadingData} title="Next page">
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
