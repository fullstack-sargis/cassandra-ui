import type { DataPage } from '../api/types';

type Props = {
  data?: DataPage;
  loading: boolean;
};

export function DataTable({ data, loading }: Props) {
  if (loading) {
    return <div className="empty-state">Loading data...</div>;
  }

  if (!data || data.columns.length === 0) {
    return <div className="empty-state">No data loaded</div>;
  }

  return (
    <div className="table-scroll">
      <table>
        <thead>
          <tr>
            {data.columns.map(column => (
              <th key={column}>{column}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.rows.map((row, index) => (
            <tr key={index}>
              {data.columns.map(column => (
                <td key={column}>{formatCell(row[column])}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function formatCell(value: unknown) {
  if (value === null || value === undefined) {
    return <span className="null-value">null</span>;
  }
  if (typeof value === 'object') {
    return JSON.stringify(value);
  }
  return String(value);
}
