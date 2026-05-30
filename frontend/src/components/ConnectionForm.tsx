import { FormEvent, useState } from 'react';
import { PlugZap } from 'lucide-react';
import type { ConnectionRequest } from '../api/types';

type Props = {
  loading: boolean;
  onConnect: (connection: ConnectionRequest) => Promise<void>;
};

export function ConnectionForm({ loading, onConnect }: Props) {
  const [connection, setConnection] = useState<ConnectionRequest>({
    host: 'localhost',
    port: 9042,
    datacenter: 'dc1',
    username: '',
    password: '',
    keyspace: '',
  });

  const update = (field: keyof ConnectionRequest, value: string) => {
    setConnection(current => ({ ...current, [field]: field === 'port' ? Number(value) : value }));
  };

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    await onConnect(connection);
  };

  return (
    <form className="connection-panel" onSubmit={submit}>
      <div className="panel-heading">
        <div>
          <span className="eyebrow">Connection</span>
          <h1>Cassandra UI</h1>
        </div>
        <button className="primary-button" disabled={loading}>
          <PlugZap size={18} />
          {loading ? 'Connecting' : 'Connect'}
        </button>
      </div>

      <div className="connection-grid">
        <label>
          Host
          <input value={connection.host} onChange={event => update('host', event.target.value)} required />
        </label>
        <label>
          Port
          <input type="number" min="1" max="65535" value={connection.port} onChange={event => update('port', event.target.value)} required />
        </label>
        <label>
          Datacenter
          <input value={connection.datacenter} onChange={event => update('datacenter', event.target.value)} required />
        </label>
        <label>
          Keyspace
          <input value={connection.keyspace} onChange={event => update('keyspace', event.target.value)} placeholder="optional" />
        </label>
        <label>
          Username
          <input value={connection.username} onChange={event => update('username', event.target.value)} placeholder="optional" />
        </label>
        <label>
          Password
          <input type="password" value={connection.password} onChange={event => update('password', event.target.value)} placeholder="optional" />
        </label>
      </div>
    </form>
  );
}
