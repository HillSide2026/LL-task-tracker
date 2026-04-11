import { apiJson, apiNoContent } from './client'

export function sendCaseEmail(keycloak, body) {
  return apiNoContent(keycloak, '/case-email', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body,
  })
}

export function findCaseEmails(keycloak, params = {}) {
  return apiJson(keycloak, '/case-email', { query: params })
}
