import { apiJson, apiNoContent } from './client'

export function findForms(keycloak) {
  return apiJson(keycloak, '/form')
}

export function getForm(keycloak, formKey) {
  return apiJson(keycloak, `/form/${encodeURIComponent(formKey)}`)
}

export function createForm(keycloak, body) {
  return apiNoContent(keycloak, '/form', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body,
  })
}

export function updateForm(keycloak, id, body) {
  return apiNoContent(keycloak, `/form/${encodeURIComponent(id)}`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    body,
  })
}

export function deleteForm(keycloak, id) {
  return apiNoContent(keycloak, `/form/${encodeURIComponent(id)}`, {
    method: 'DELETE',
  })
}

export function findVariables(keycloak, processInstanceId) {
  return apiJson(keycloak, '/variable', {
    query: {
      processInstanceId,
    },
  })
}
