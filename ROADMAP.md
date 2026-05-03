# Roadmap

## Levine LLP Matter Management App

- Set up a new repository for the Levine LLP matter management app and deployment work.
- Host the matter management app and its associated workflows at `tasks.levinellp.ca`.
- Require team members to sign in and perform their relevant workflow tasks through the matter management app.
- Use the matter management app to track, control, manage, and ensure execution of:
  - all Levine Law (LL) matters
  - corporate services matters, with the client user experience hosted at `levinellp.ca/corporate`

## Staging Deployment Roadmap

- Deploy only the React case portal frontend to Vercel, with the project rooted at `apps/react/case-portal`, build command `npm run build`, and output directory `dist`.
- Host the backend stack on Docker-capable infrastructure rather than Vercel, because the operational platform depends on long-running services: Keycloak, Spring Boot APIs, MongoDB, MinIO, OPA, Camunda 7, and the Camunda 7 external task worker.
- Use staging domains that separate frontend hosting from backend services:
  - `staging-tasks.levinellp.ca` for the Vercel-hosted React portal
  - `staging-api.tasks.levinellp.ca` for backend routes such as `/auth`, `/api`, and `/storage`
  - `staging-s3.tasks.levinellp.ca` for browser-facing object storage URLs
- Keep the minimum viable staging stack focused on authenticated matter workflow review, with websocket, Novu, Kafka, and email-to-case disabled unless they become part of the staging acceptance scope.
- Seed staging with the Levine-focused demo-data-loader bootstrap: Keycloak realm/client/users/roles, Mongo shared collections, Levine matter definitions/forms/queues, and Camunda 7 workflow definitions.
- Configure the Vercel frontend to reach the backend through public staging URLs for Keycloak, case API, and storage API, and ensure Keycloak redirect URIs and web origins include the Vercel staging domain.
- Before relying on Vercel production environment variables, update the frontend config strategy so production builds can read Vercel-provided `REACT_APP_*` values or another explicit public runtime config. The current Docker-oriented placeholder approach is not sufficient for Vercel by itself.
