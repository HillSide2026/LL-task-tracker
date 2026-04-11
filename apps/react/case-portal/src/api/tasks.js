import { apiJson, apiNoContent } from './client'

export function getActivityInstances(keycloak, processInstanceId) {
  return apiJson(
    keycloak,
    `/process-instance/${encodeURIComponent(processInstanceId)}/activity-instances`,
  )
}

export function claimTask(keycloak, taskId, taskAssignee) {
  return apiNoContent(
    keycloak,
    `/task/${encodeURIComponent(taskId)}/claim/${encodeURIComponent(
      taskAssignee,
    )}`,
    {
      method: 'POST',
    },
  )
}

export function unclaimTask(keycloak, taskId) {
  return apiNoContent(keycloak, `/task/${encodeURIComponent(taskId)}/unclaim`, {
    method: 'POST',
  })
}

export function completeTask(keycloak, taskId, body) {
  return apiNoContent(
    keycloak,
    `/task/${encodeURIComponent(taskId)}/complete`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body,
    },
  )
}

export function createTask(keycloak, body) {
  return apiNoContent(keycloak, '/task/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body,
  })
}

export function findTasks(keycloak, params = {}) {
  return apiJson(keycloak, '/task', { query: params })
}

export function findProcessInstances(keycloak, params = {}) {
  return apiJson(keycloak, '/process-instance', { query: params })
}
