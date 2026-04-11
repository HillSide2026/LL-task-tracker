import { EmailApi } from '../api'

export const EmailService = {
  send,
  getAllByBusinessKey,
}

async function send(keycloak, body) {
  try {
    return EmailApi.sendCaseEmail(keycloak, body)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function getAllByBusinessKey(keycloak, caseInstanceBusinessKey) {
  try {
    return EmailApi.findCaseEmails(keycloak, { caseInstanceBusinessKey })
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}
