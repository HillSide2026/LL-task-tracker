# Levine LLP Matter Portal

React application for Levine LLP's internal matter portal.

The app still uses the inherited case/task/record management shell, but visible branding and matter-specific routes are Levine LLP focused.

## Local Development

```sh
npm install
npm run start
```

## Configuration

Runtime URLs are injected through the `REACT_APP_*` variables for local development and `__SERVER_*__` placeholders for the containerized deployment.

The Keycloak client ID remains `wks-portal` for compatibility until the auth client cleanup phase.

## Matter Operations

Matter-specific UI is concentrated in:

- `src/common/adminLifecycle.js`
- `src/routes/MainRoutes.js`
- `src/views/dashboard`
- `src/views/caseList/caseList.js`
- `src/views/caseForm/caseForm.js`
