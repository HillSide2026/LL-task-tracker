import { apiNoContent } from './client'

export function deployProcessDefinition(keycloak, file) {
  return apiNoContent(keycloak, '/deployment', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: file,
  })
}
