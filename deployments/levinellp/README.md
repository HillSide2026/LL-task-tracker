# Levine LLP Matter Portal Deployment

This folder is a first-pass deployment scaffold for running the customized WKS matter-admin app at:

- Portal: `https://matters.levinellp.ca`
- Object storage API: `https://s3.matters.levinellp.ca`
- Keycloak: `https://matters.levinellp.ca/auth`
- Case API: `https://matters.levinellp.ca/engine`
- Storage API: `https://matters.levinellp.ca/storage`

The compose stack uses Camunda 7 because it is the lighter WKS runtime and is the fastest path to a working matter portal. It uses Traefik and Let's Encrypt for HTTPS.

## DNS

Create DNS records before starting the stack:

```text
matters.levinellp.ca      A or CNAME -> deployment host
s3.matters.levinellp.ca   A or CNAME -> deployment host
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

- `APP_HOST=matters.levinellp.ca`
- `S3_HOST=s3.matters.levinellp.ca`
- `TENANT_REALM=matters`
- `LETSENCRYPT_EMAIL=admin@levinellp.ca`
- `KEYCLOAK_ADMIN_PASSWORD`
- `KEYCLOAK_DEFAULT_USER_PASSWORD`
- `KEYCLOAK_EXTERNALTASKS_SECRET`
- `MINIO_ROOT_PASSWORD`

The React portal derives its Keycloak realm from the first hostname segment. For `matters.levinellp.ca`, the realm must be `matters`.

## Start

From the repository root:

```sh
docker compose \
  --env-file deployments/levinellp/.env \
  -f deployments/levinellp/docker-compose.camunda7.yml \
  up -d --build
```

Bootstrap the initial realm, roles, demo user, case definitions, forms, and matter-admin sample process:

```sh
docker compose \
  --env-file deployments/levinellp/.env \
  -f deployments/levinellp/docker-compose.camunda7.yml \
  --profile bootstrap \
  run --rm demo-data-loader
```

Then visit:

```text
https://matters.levinellp.ca
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
https://matters.levinellp.ca/auth
https://matters.levinellp.ca/engine/healthCheck
https://s3.matters.levinellp.ca/minio/health/live
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
