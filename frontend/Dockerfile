FROM node:22-alpine AS build
RUN apk add --no-cache ca-certificates openssl \
    && for host in registry.npmjs.org; do \
        export CERT_HOST="$host"; \
        openssl s_client -showcerts -connect "$host:443" -servername "$host" </dev/null 2>/dev/null \
          | awk 'BEGIN {n=0} /BEGIN CERTIFICATE/ {n++} n > 0 {print > "/usr/local/share/ca-certificates/" ENVIRON["CERT_HOST"] "-" n ".crt"}'; \
      done \
    && update-ca-certificates
ENV NODE_EXTRA_CA_CERTS=/etc/ssl/certs/ca-certificates.crt
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
