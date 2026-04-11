# Demo Data Loader Seeds

The default loader path imports files recursively from `camunda7` or `camunda8` and imports every JSON file directly under `mongodb`.

The default seed set is now Levine-focused: platform bootstrap process definitions plus Levine LLP matter definitions, forms, and queues.

## Levine LLP Matter Seeds

- `camunda7/levinellp/matter-admin-opening-control.bpmn`
- `camunda8/levinellp/matter-admin-opening-control.bpmn`
- `mongodb/mongo-levinellp-matter-collections.json`

These files define the Levine LLP matter-admin opening and maintenance lifecycle.

## Upstream Demo Seeds

- `upstream-demo/camunda7`
- `upstream-demo/camunda8`
- `upstream-demo/mongodb/mongo-upstream-demo-collections.json`

These files are retained as inherited sample workflows and are not imported by the default Levine loader path. Review them before enabling them in any Levine environment.

## Platform Bootstrap Seeds

- `camunda7/case-instance-create.bpmn`
- `camunda8/case-instance-create.bpmn`
- `mongodb/mongo-shared-collections.json`

These are generic platform/bootstrap files.
