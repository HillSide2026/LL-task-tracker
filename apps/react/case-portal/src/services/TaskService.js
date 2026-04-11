import { TaskApi } from '../api'

export const TaskService = {
  getActivityInstancesById,
  claim,
  unclaim,
  complete,
  createNewTask,
  filterTasks,
  filterProcessInstances,
}

async function getActivityInstancesById(keycloak, processInstanceId) {
  try {
    return TaskApi.getActivityInstances(keycloak, processInstanceId)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function claim(keycloak, taskId) {
  try {
    return TaskApi.claimTask(
      keycloak,
      taskId,
      keycloak.idTokenParsed.given_name,
    )
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function unclaim(keycloak, taskId) {
  try {
    return TaskApi.unclaimTask(keycloak, taskId)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function complete(keycloak, taskId, body) {
  try {
    return TaskApi.completeTask(keycloak, taskId, body)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function createNewTask(keycloak, body) {
  try {
    return TaskApi.createTask(keycloak, body)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function filterTasks(keycloak, businessKey) {
  try {
    return TaskApi.findTasks(keycloak, { businessKey })
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function filterProcessInstances(keycloak, businessKey = '') {
  try {
    return TaskApi.findProcessInstances(keycloak, { businessKey })
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
