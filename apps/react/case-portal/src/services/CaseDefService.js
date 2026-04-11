import { CaseDefinitionApi } from '../api'

export const CaseDefService = {
  create,
  update,
  remove,
  getAll,
}

async function create(keycloak, body) {
  try {
    return CaseDefinitionApi.createCaseDefinition(keycloak, body)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function update(keycloak, id, body) {
  try {
    return CaseDefinitionApi.updateCaseDefinition(keycloak, id, body)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function remove(keycloak, id) {
  try {
    return CaseDefinitionApi.deleteCaseDefinition(keycloak, id)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function getAll(keycloak) {
  try {
    return CaseDefinitionApi.findCaseDefinitions(keycloak)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}
