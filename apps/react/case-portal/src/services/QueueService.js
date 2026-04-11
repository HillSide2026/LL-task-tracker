import { QueueApi } from '../api'

export const QueueService = {
  find,
  get,
  update,
  remove,
  save,
}

async function save(keycloak, body) {
  try {
    return QueueApi.createQueue(keycloak, body)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function update(keycloak, id, body) {
  try {
    return QueueApi.updateQueue(keycloak, id, body)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function remove(keycloak, id) {
  try {
    return QueueApi.deleteQueue(keycloak, id)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function find(keycloak) {
  try {
    return QueueApi.findQueues(keycloak)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function get(keycloak, id) {
  try {
    return QueueApi.getQueue(keycloak, id)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}
