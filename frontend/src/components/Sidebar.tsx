import { Database, Plus, Table2, Trash2 } from 'lucide-react';
import { useState } from 'react';
import type { Keyspace, Table } from '../api/types';

type Props = {
  keyspaces: Keyspace[];
  tables: Table[];
  selectedKeyspace?: string;
  selectedTable?: string;
  loadingTables: boolean;
  savingKeyspace: boolean;
  onSelectKeyspace: (keyspace: string) => void;
  onSelectTable: (table: string) => void;
  onCreateKeyspace: (name: string, replicationFactor: number, durableWrites: boolean) => Promise<void>;
  onDropKeyspace: (keyspace: string) => Promise<void>;
};

export function Sidebar({
  keyspaces,
  tables,
  selectedKeyspace,
  selectedTable,
  loadingTables,
  savingKeyspace,
  onSelectKeyspace,
  onSelectTable,
  onCreateKeyspace,
  onDropKeyspace,
}: Props) {
  const [newKeyspace, setNewKeyspace] = useState('');
  const [replicationFactor, setReplicationFactor] = useState(1);
  const [durableWrites, setDurableWrites] = useState(true);
  const selectedKeyspaceMeta = keyspaces.find(keyspace => keyspace.name === selectedKeyspace);

  const createKeyspace = async () => {
    await onCreateKeyspace(newKeyspace, replicationFactor, durableWrites);
    setNewKeyspace('');
  };

  return (
    <aside className="sidebar">
      <section>
        <div className="section-title">
          <Database size={16} />
          Keyspaces
        </div>
        <div className="nav-list">
          {keyspaces.map(keyspace => (
            <button
              key={keyspace.name}
              className={keyspace.name === selectedKeyspace ? 'nav-item active' : 'nav-item'}
              onClick={() => onSelectKeyspace(keyspace.name)}
            >
              <span>{keyspace.name}</span>
              {keyspace.system && <small>system</small>}
            </button>
          ))}
        </div>
        <div className="keyspace-form">
          <label>
            New keyspace
            <input value={newKeyspace} onChange={event => setNewKeyspace(event.target.value)} placeholder="app_data" />
          </label>
          <label>
            Replication factor
            <input
              type="number"
              min="1"
              max="10"
              value={replicationFactor}
              onChange={event => setReplicationFactor(Number(event.target.value))}
            />
          </label>
          <label className="checkbox-label">
            <input
              type="checkbox"
              checked={durableWrites}
              onChange={event => setDurableWrites(event.target.checked)}
            />
            Durable writes
          </label>
          <div className="keyspace-actions">
            <button type="button" className="primary-button compact-button" onClick={createKeyspace} disabled={savingKeyspace || !newKeyspace.trim()}>
              <Plus size={16} />
              Create
            </button>
            <button
              type="button"
              className="danger-button"
              onClick={() => selectedKeyspace && onDropKeyspace(selectedKeyspace)}
              disabled={savingKeyspace || !selectedKeyspace || selectedKeyspaceMeta?.system}
              title="Drop selected keyspace"
            >
              <Trash2 size={16} />
            </button>
          </div>
        </div>
      </section>

      <section>
        <div className="section-title">
          <Table2 size={16} />
          Tables
        </div>
        <div className="nav-list compact">
          {loadingTables && <div className="muted-block">Loading tables...</div>}
          {!loadingTables && selectedKeyspace && tables.length === 0 && <div className="muted-block">No tables</div>}
          {tables.map(table => (
            <button
              key={table.name}
              className={table.name === selectedTable ? 'nav-item active' : 'nav-item'}
              onClick={() => onSelectTable(table.name)}
            >
              <span>{table.name}</span>
            </button>
          ))}
        </div>
      </section>
    </aside>
  );
}
