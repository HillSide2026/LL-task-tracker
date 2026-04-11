import { RecordApi } from '../api'

export const RecordTypeService = {
  create,
  update,
  remove,
  getAll,
}

async function getAll(keycloak) {
  try {
    return RecordApi.getRecordTypes(keycloak)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function create(keycloak, data) {
  try {
    return RecordApi.createRecordType(keycloak, data)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function update(keycloak, id, data) {
  try {
    return RecordApi.updateRecordType(keycloak, id, data)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function remove(keycloak, id) {
  try {
    return RecordApi.deleteRecordType(keycloak, id)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
