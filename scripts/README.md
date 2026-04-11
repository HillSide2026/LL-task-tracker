# Local Developer Scripts

These helpers start and stop the root `docker-compose*.yaml` developer stacks.

They are not the Levine LLP deployment entrypoint. For deployment-facing instructions, use `deployments/levinellp/README.md`.

Several script targets still compose inherited platform services such as Camunda 8, Novu, websocket publishing, and email-to-case. Review those services before treating them as part of the Levine production operating model.
