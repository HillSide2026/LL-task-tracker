import { apiJson, apiNoContent } from './client'

export function getMatterDefinitions(keycloak, params = {}) {
  return apiJson(keycloak, '/case-definition', { query: params })
}

export function getMatterDefinition(keycloak, caseDefId) {
  return apiJson(keycloak, `/case-definition/${encodeURIComponent(caseDefId)}`)
}

export function getMatters(keycloak, params = {}) {
  return apiJson(keycloak, '/case', { query: params })
}

export function getMatter(keycloak, businessKey) {
  return apiJson(keycloak, `/case/${encodeURIComponent(businessKey)}`)
}

export function createMatter(keycloak, body) {
  return apiJson(keycloak, '/case', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body,
  })
}

export function patchMatter(keycloak, businessKey, body) {
  return apiNoContent(keycloak, `/case/${encodeURIComponent(businessKey)}`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/merge-patch+json',
    },
    body,
  })
}

export function transitionMatter(
  keycloak,
  businessKey,
  transitionName,
  body = {},
) {
  return apiJson(
    keycloak,
    `/case/${encodeURIComponent(businessKey)}/transition/${encodeURIComponent(
      transitionName,
    )}`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body,
    },
  )
}

export function addMatterDocument(keycloak, businessKey, document) {
  return apiNoContent(
    keycloak,
    `/case/${encodeURIComponent(businessKey)}/document`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: document,
    },
  )
}

export function addMatterComment(keycloak, businessKey, comment) {
  return apiNoContent(
    keycloak,
    `/case/${encodeURIComponent(businessKey)}/comment`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: comment,
    },
  )
}

export function updateMatterComment(keycloak, businessKey, commentId, comment) {
  return apiNoContent(
    keycloak,
    `/case/${encodeURIComponent(businessKey)}/comment/${encodeURIComponent(
      commentId,
    )}`,
    {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: comment,
    },
  )
}

export function deleteMatterComment(keycloak, businessKey, commentId) {
  return apiNoContent(
    keycloak,
    `/case/${encodeURIComponent(businessKey)}/comment/${encodeURIComponent(
      commentId,
    )}`,
    {
      method: 'DELETE',
    },
  )
}
