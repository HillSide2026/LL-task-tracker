import { FormApi } from '../api'

export const VariableService = {
  getByProcessInstanceId,
}

async function getByProcessInstanceId(keycloak, processInstanceId) {
  try {
    return FormApi.findVariables(keycloak, processInstanceId)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}
