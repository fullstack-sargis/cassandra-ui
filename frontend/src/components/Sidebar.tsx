import { Database, Table2 } from 'lucide-react';
import type { Keyspace, Table } from '../api/types';

type Props = {
  keyspaces: Keyspace[];
  tables: Table[];
  selectedKeyspace?: string;
  selectedTable?: string;
  loadingTables: boolean;
  onSelectKeyspace: (keyspace: string) => void;
  onSelectTable: (table: string) => void;
};

export function Sidebar({ keyspaces, tables, selectedKeyspace, selectedTable, loadingTables, onSelectKeyspace, onSelectTable }: Props) {
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
