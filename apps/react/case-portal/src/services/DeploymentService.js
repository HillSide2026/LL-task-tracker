import { DeploymentApi } from '../api'

export const DeploymentService = {
  deploy,
}

async function deploy(keycloak, file) {
  try {
    return DeploymentApi.deployProcessDefinition(keycloak, file)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}
