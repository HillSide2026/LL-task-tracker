# Levine LLP Matter Portal Deployment

This folder is the deployment source of truth for running Levine LLP's matter platform at:

- Portal: `https://tasks.levinellp.ca`
- Object storage API: `https://s3.tasks.levinellp.ca`
- Keycloak: `https://tasks.levinellp.ca/auth`
- Case API: `https://tasks.levinellp.ca/api`
- Storage API: `https://tasks.levinellp.ca/storage`

The compose stack uses Camunda 7 because it is the current Levine workflow runtime and the fastest path to a working matter portal. It uses Traefik and Let's Encrypt for HTTPS.

Some internal service identifiers still retain upstream WKS names for compatibility. Those names are implementation details and should not be treated as the product identity.

## DNS

Create DNS records before starting the stack:

```text
tasks.levinellp.ca      A or CNAME -> deployment host
s3.tasks.levinellp.ca   A or CNAME -> deployment host
```

The companion `s3` hostname is required because document uploads and downloads use browser-facing pre-signed object-storage URLs.

## Host Setup

Use an Ubuntu VPS or similar host with:

- Docker Engine
- Docker Compose v2
- ports `80` and `443` open to the internet
- enough memory for MongoDB, Keycloak, Camunda 7, the Java services, and the React portal

Build the Java service jars before building the Docker images:

```sh
cd apps/java
mvn -DskipTests package
cd ../..
```

## Configure

Copy the template and replace all `change-me-*` values:

```sh
cp deployments/levinellp/.env.example deployments/levinellp/.env
```

Important values:

- `APP_HOST=tasks.levinellp.ca`
- `S3_HOST=s3.tasks.levinellp.ca`
- `TENANT_REALM=tasks`
- `LETSENCRYPT_EMAIL=admin@levinellp.ca`
- `KEYCLOAK_ADMIN_PASSWORD`
- `KEYCLOAK_DEFAULT_USER_PASSWORD`
- `KEYCLOAK_EXTERNALTASKS_SECRET`
- `MINIO_ROOT_PASSWORD`

The React portal derives its Keycloak realm from the first hostname segment. For `tasks.levinellp.ca`, the realm must be `tasks`.

## Start

From the repository root:

```sh
docker compose \
  --env-file deployments/levinellp/.env \
  -f deployments/levinellp/docker-compose.camunda7.yml \
  up -d --build
```

Bootstrap the initial realm, roles, demo user, case definitions, forms, and matter-admin process:

```sh
docker compose \
  --env-file deployments/levinellp/.env \
  -f deployments/levinellp/docker-compose.camunda7.yml \
  --profile bootstrap \
  run --rm demo-data-loader
```

Then visit:

```text
https://tasks.levinellp.ca
```

## Verify

Check the running containers:

```sh
docker compose \
  --env-file deployments/levinellp/.env \
  -f deployments/levinellp/docker-compose.camunda7.yml \
  ps
```

Useful health URLs:

```text
https://tasks.levinellp.ca/auth
https://tasks.levinellp.ca/api/healthCheck
https://s3.tasks.levinellp.ca/minio/health/live
```

## Production Notes

This gets the app onto the domain as a deployable pilot stack. Before using it for confidential client matter data, harden it:

- replace all template secrets with strong values
- move Keycloak off `start-dev` to a proper production Keycloak configuration
- add backups for MongoDB, Keycloak data, MinIO data, and Traefik ACME certs
- restrict host firewall access to `80` and `443`
- confirm MinIO CORS and file upload/download behavior in-browser
- decide whether MinIO should remain self-hosted or move to managed object storage
- decide whether MongoDB should remain self-hosted or move to managed MongoDB

## Compatibility Notes

- The compose project name is `levine-matter-platform`.
- OPA still serves the inherited policy path `/v1/data/wks/authz/allow`.
- The external-task worker still reads `WKS_CASE_API_URL`, `WKS_CLIENT_ID`, and `WKS_CLIENT_SECRET`.
- The Keycloak portal client is still `wks-portal`.
- The legacy `/engine` reverse-proxy path is retained as a temporary alias while the portal standardizes on `/api`.

These identifiers are deferred internals. Keep deployment instructions and user-facing copy aligned to Levine LLP's matter platform.
