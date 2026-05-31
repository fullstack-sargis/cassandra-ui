import { Play } from 'lucide-react';
import { useState } from 'react';

type Props = {
  loading: boolean;
  onRun: (query: string) => Promise<void>;
};

export function QueryEditor({ loading, onRun }: Props) {
  const [query, setQuery] = useState('SELECT * FROM system.local;');

  return (
    <div className="query-editor">
      <div className="query-toolbar">
        <button className="primary-button" onClick={() => onRun(query)} disabled={loading}>
          <Play size={17} />
          {loading ? 'Executing' : 'Execute'}
        </button>
      </div>
      <textarea value={query} onChange={event => setQuery(event.target.value)} spellCheck={false} />
    </div>
  );
}
