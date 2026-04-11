import { apiJson, apiText } from './client'

export function startProcessDefinition(keycloak, procDefKey, businessKey) {
  return apiJson(
    keycloak,
    `/process-definition/key/${encodeURIComponent(procDefKey)}/start`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: {
        businessKey,
      },
    },
  )
}

export function findProcessDefinitions(keycloak) {
  return apiJson(keycloak, '/process-definition')
}

export function getProcessDefinitionXml(keycloak, processDefId) {
  return apiText(
    keycloak,
    `/process-definition/${encodeURIComponent(processDefId)}/xml`,
    {
      headers: {
        Accept: 'application/xml',
      },
    },
  )
}
