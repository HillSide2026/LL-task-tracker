import { apiJson, apiNoContent } from './client'

export function findQueues(keycloak) {
  return apiJson(keycloak, '/queue')
}

export function getQueue(keycloak, id) {
  return apiJson(keycloak, `/queue/${encodeURIComponent(id)}`)
}

export function createQueue(keycloak, body) {
  return apiNoContent(keycloak, '/queue', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body,
  })
}

export function updateQueue(keycloak, id, body) {
  return apiNoContent(keycloak, `/queue/${encodeURIComponent(id)}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body,
  })
}

export function deleteQueue(keycloak, id) {
  return apiNoContent(keycloak, `/queue/${encodeURIComponent(id)}`, {
    method: 'DELETE',
  })
}
