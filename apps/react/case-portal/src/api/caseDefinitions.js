import { apiJson, apiNoContent } from './client'

export function findCaseDefinitions(keycloak, params = {}) {
  return apiJson(keycloak, '/case-definition', { query: params })
}

export function createCaseDefinition(keycloak, body) {
  return apiNoContent(keycloak, '/case-definition', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body,
  })
}

export function updateCaseDefinition(keycloak, id, body) {
  return apiNoContent(keycloak, `/case-definition/${encodeURIComponent(id)}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body,
  })
}

export function deleteCaseDefinition(keycloak, id) {
  return apiNoContent(keycloak, `/case-definition/${encodeURIComponent(id)}`, {
    method: 'DELETE',
  })
}
