import { Play } from 'lucide-react';
import { useState } from 'react';

type Props = {
  loading: boolean;
  onRun: (query: string, pageSize: number) => Promise<void>;
};

export function QueryEditor({ loading, onRun }: Props) {
  const [query, setQuery] = useState('SELECT * FROM system.local;');
  const [pageSize, setPageSize] = useState(50);

  return (
    <div className="query-editor">
      <div className="query-toolbar">
        <label>
          Page size
          <input type="number" min="1" max="500" value={pageSize} onChange={event => setPageSize(Number(event.target.value))} />
        </label>
        <button className="primary-button" onClick={() => onRun(query, pageSize)} disabled={loading}>
          <Play size={17} />
          {loading ? 'Running' : 'Run'}
        </button>
      </div>
      <textarea value={query} onChange={event => setQuery(event.target.value)} spellCheck={false} />
    </div>
  );
}
