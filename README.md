# Cassandra UI

A Docker-packaged Cassandra browser with a React 19 + TypeScript frontend and a Java 21 Spring Boot backend.

## Features

- Connect to Cassandra with host, port, datacenter, optional username/password, and optional keyspace.
- Browse keyspaces and tables.
- Inspect table schema and generated CQL.
- Browse table data with page controls.
- Run custom `SELECT` queries only.
- Backend blocks mutation and schema-changing query keywords.

## Run With Docker

```bash
docker compose up --build
```

Open the frontend at:

```text
http://localhost:3000
```

Backend API is exposed at:

```text
http://localhost:8080
```

## Compose Cassandra Connection Values

Use these values in the connection form when running through Docker Compose:

```text
Host: cassandra
Port: 9042
Datacenter: dc1
Username: leave blank
Password: leave blank
Keyspace: optional
```

If you run the frontend locally outside Docker and connect to the compose Cassandra port from your host machine:

```text
Host: localhost
Port: 9042
Datacenter: dc1
```

## Local Development

Start Cassandra and the backend dependencies:

```bash
docker compose up cassandra
```

Run the backend:

```bash
cd backend
gradle bootRun
```

Run the frontend:

```bash
cd frontend
npm install
npm run dev
```

Open:

```text
http://localhost:5173
```

The Vite dev server proxies `/api` to `http://localhost:8080`.

## API

- `POST /api/connections/test`
- `GET /api/keyspaces`
- `GET /api/keyspaces/{keyspace}/tables`
- `GET /api/keyspaces/{keyspace}/tables/{table}/schema`
- `GET /api/keyspaces/{keyspace}/tables/{table}/data?page=&size=`
- `POST /api/query`

`POST /api/query` accepts only a single `SELECT` statement. Queries containing mutation or schema-changing keywords such as `INSERT`, `UPDATE`, `DELETE`, `DROP`, `TRUNCATE`, `ALTER`, or `CREATE` are rejected.
