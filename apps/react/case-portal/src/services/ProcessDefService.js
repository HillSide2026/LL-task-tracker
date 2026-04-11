import { ProcessDefinitionApi } from '../api'

export const ProcessDefService = {
  start,
  find,
  getBPMNXml,
}

async function start(keycloak, procDefKey, businessKey) {
  try {
    return ProcessDefinitionApi.startProcessDefinition(
      keycloak,
      procDefKey,
      businessKey,
    )
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function find(keycloak) {
  try {
    return ProcessDefinitionApi.findProcessDefinitions(keycloak)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function getBPMNXml(keycloak, processDefId) {
  try {
    return ProcessDefinitionApi.getProcessDefinitionXml(keycloak, processDefId)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}
