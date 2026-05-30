import type { TableSchema } from '../api/types';

type Props = {
  schema?: TableSchema;
  loading: boolean;
};

export function SchemaViewer({ schema, loading }: Props) {
  if (loading) {
    return <div className="empty-state">Loading schema...</div>;
  }

  if (!schema) {
    return <div className="empty-state">Select a table to inspect its schema</div>;
  }

  return (
    <div className="schema-layout">
      <div className="table-scroll">
        <table>
          <thead>
            <tr>
              <th>Column</th>
              <th>Type</th>
              <th>Kind</th>
            </tr>
          </thead>
          <tbody>
            {schema.columns.map(column => (
              <tr key={column.name}>
                <td>{column.name}</td>
                <td>{column.type}</td>
                <td><span className="tag">{column.kind}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <pre className="code-block">{schema.createStatement}</pre>
    </div>
  );
}
