import { apiJson, apiNoContent } from './client'

export function getRecords(keycloak, recordTypeId) {
  return apiJson(keycloak, `/record/${encodeURIComponent(recordTypeId)}`)
}

export function getRecord(keycloak, recordTypeId, id) {
  return apiJson(
    keycloak,
    `/record/${encodeURIComponent(recordTypeId)}/${encodeURIComponent(id)}`,
  )
}

export function createRecord(keycloak, recordTypeId, data) {
  return apiNoContent(keycloak, `/record/${encodeURIComponent(recordTypeId)}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: data,
  })
}

export function updateRecord(keycloak, recordTypeId, id, data) {
  return apiNoContent(
    keycloak,
    `/record/${encodeURIComponent(recordTypeId)}/${encodeURIComponent(id)}`,
    {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
      },
      body: data,
    },
  )
}

export function deleteRecord(keycloak, recordTypeId, id) {
  return apiNoContent(
    keycloak,
    `/record/${encodeURIComponent(recordTypeId)}/${encodeURIComponent(id)}`,
    {
      method: 'DELETE',
    },
  )
}

export function getRecordTypes(keycloak) {
  return apiJson(keycloak, '/record-type')
}

export function getRecordType(keycloak, id) {
  return apiJson(keycloak, `/record-type/${encodeURIComponent(id)}`)
}

export function createRecordType(keycloak, data) {
  return apiNoContent(keycloak, '/record-type', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: data,
  })
}

export function updateRecordType(keycloak, id, data) {
  return apiNoContent(keycloak, `/record-type/${encodeURIComponent(id)}`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    body: data,
  })
}

export function deleteRecordType(keycloak, id) {
  return apiNoContent(keycloak, `/record-type/${encodeURIComponent(id)}`, {
    method: 'DELETE',
  })
}
