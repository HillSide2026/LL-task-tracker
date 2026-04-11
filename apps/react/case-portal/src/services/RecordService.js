import { RecordApi } from '../api'

export const RecordService = {
  getRecordTypeById,
  getAllRecordTypes,
  createRecordType,
  getRecordById,
  updateRecord,
  createRecord,
  deleteRecord,
}

async function getRecordById(keycloak, id) {
  try {
    return RecordApi.getRecords(keycloak, id)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function getAllRecordTypes(keycloak) {
  try {
    return RecordApi.getRecordTypes(keycloak)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function getRecordTypeById(keycloak, id) {
  try {
    return RecordApi.getRecordType(keycloak, id)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function createRecordType(keycloak, id, data) {
  try {
    return RecordApi.createRecordType(keycloak, data)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function createRecord(keycloak, id, data) {
  try {
    return RecordApi.createRecord(keycloak, id, data)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function updateRecord(keycloak, id, oid, data) {
  try {
    return RecordApi.updateRecord(keycloak, id, oid, data)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function deleteRecord(keycloak, id, oid) {
  try {
    return RecordApi.deleteRecord(keycloak, id, oid)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}
