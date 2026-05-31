# Cassandra UI

A Docker-packaged Cassandra browser with a React 19 + TypeScript frontend and a Java 21 Spring Boot backend.

## Features

- Connect to Cassandra with host, port, datacenter, optional username/password, and optional keyspace.
- Browse keyspaces and tables.
- Create and drop keyspaces.
- Inspect table schema and generated CQL.
- Browse table data with page controls.
- Execute single-statement Cassandra CQL from the Query tab.

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

## Cassandra Connection Values

Docker Compose starts only the Cassandra UI frontend and backend. Use the connection form to connect to an existing Cassandra instance.

```text
Host: your Cassandra host
Port: 9042
Datacenter: your datacenter name
Username: optional
Password: optional
Keyspace: optional
```

If Cassandra is running on your host machine and the backend is running in Docker Compose, use:

```text
Host: host.docker.internal
Port: 9042
Datacenter: your datacenter name
```

## Local Development

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
- `POST /api/keyspaces`
- `DELETE /api/keyspaces/{keyspace}`
- `GET /api/keyspaces/{keyspace}/tables`
- `GET /api/keyspaces/{keyspace}/tables/{table}/schema`
- `GET /api/keyspaces/{keyspace}/tables/{table}/data?page=&size=`
- `POST /api/query`

`POST /api/query` accepts one Cassandra CQL statement. Statements that return rows are returned as tabular data; mutation and schema statements return a success message.
