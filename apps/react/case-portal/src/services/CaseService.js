import { MatterApi } from '../api'
import i18n from '../i18n'
import { getAdminStateLabel } from '../common/adminLifecycle'

export const CaseService = {
  getAllByStatus,
  getCaseDefinitions,
  getCaseDefinitionsById,
  getCaseById,
  filterCase,
  createCase,
  patch,
  transition,
  addDocuments,
  addComment,
  updateComment,
  deleteComment,
}

async function getAllByStatus(keycloak, status, limit) {
  if (!status) {
    return Promise.resolve([])
  }

  try {
    const data = await MatterApi.getMatters(keycloak, { status, limit })
    return mapperToCase(data)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function getCaseDefinitions(keycloak) {
  try {
    return MatterApi.getMatterDefinitions(keycloak, { deployed: true })
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function getCaseDefinitionsById(keycloak, caseDefId) {
  try {
    return MatterApi.getMatterDefinition(keycloak, caseDefId || '')
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function getCaseById(keycloak, id) {
  try {
    const data = await MatterApi.getMatter(keycloak, id)
    return mapCaseItem(data)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function filterCase(keycloak, caseDefId, status, cursor, filters = {}) {
  const params = {
    status,
    caseDefinitionId: caseDefId,
    adminState: filters.adminState,
    adminHealth: filters.adminHealth,
    nextActionOwnerType: filters.nextActionOwnerType,
    queueId: filters.queueId,
    malformedCase: filters.malformedCase,
    exceptionOnly: filters.exceptionOnly,
    adminOwnerId: filters.adminOwnerId,
    healthReasonCode: filters.healthReasonCode,
    before: cursor.before,
    after: cursor.after,
    sort: cursor.sort || 'desc',
    limit: cursor.limit || 10,
  }

  try {
    const data = await MatterApi.getMatters(keycloak, params)
    return mapperToCase(data)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function patch(keycloak, id, body) {
  try {
    return MatterApi.patchMatter(keycloak, id, body)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function transition(keycloak, businessKey, transitionName, body = {}) {
  try {
    return MatterApi.transitionMatter(
      keycloak,
      businessKey,
      transitionName,
      body,
    )
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function createCase(keycloak, body) {
  try {
    const data = await MatterApi.createMatter(keycloak, body)
    return mapCaseItem(data)
  } catch (err) {
    console.log(err)
    return await Promise.reject(err)
  }
}

async function addDocuments(keycloak, businessKey, document) {
  try {
    return MatterApi.addMatterDocument(keycloak, businessKey, document)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function addComment(keycloak, text, parentId, businessKey) {
  const comment = {
    body: text,
    parentId,
    userId: keycloak.tokenParsed.preferred_username,
    userName: keycloak.tokenParsed.given_name,
    caseId: businessKey,
  }

  try {
    return MatterApi.addMatterComment(keycloak, businessKey, comment)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function updateComment(keycloak, text, commentId, businessKey) {
  const comment = {
    id: commentId,
    body: text,
    userId: keycloak.tokenParsed.preferred_username,
    caseId: businessKey,
  }

  try {
    return MatterApi.updateMatterComment(
      keycloak,
      businessKey,
      commentId,
      comment,
    )
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

async function deleteComment(keycloak, commentId, businessKey) {
  try {
    return MatterApi.deleteMatterComment(keycloak, businessKey, commentId)
  } catch (e) {
    console.log(e)
    return await Promise.reject(e)
  }
}

function mapperToCase(resp) {
  const { data, paging } = resp

  if (!data.length) {
    return Promise.resolve({ data: [], paging: {} })
  }

  const toCase = data.map((element, index) => mapCaseItem(element, index))

  const toPaging = {
    cursors: paging.cursors,
    hasPrevious: paging.hasPrevious,
    hasNext: paging.hasNext,
  }

  return Promise.resolve({ data: toCase, paging: toPaging })
}

function mapCaseItem(element, index = 0) {
  if (!element) {
    return element
  }

  const toStatus = (status) => {
    const mapper = {
      WIP_CASE_STATUS: i18n.t('general.case.status.wip'),
      CLOSED_CASE_STATUS: i18n.t('general.case.status.closed'),
      ARCHIVED_CASE_STATUS: i18n.t('general.case.status.archived'),
    }

    return mapper[status] || '-'
  }

  const createdAt = element?.attributes?.find(
    (attribute) => attribute.name === 'createdAt',
  )
  element.createdAt = createdAt ? createdAt.value : ''
  element.statusDescription = toStatus(element.status)
  element.adminHealthDescription = element.adminHealth || '-'
  element.adminStateDescription = getAdminStateLabel(element.adminState)
  element.resumeToStateDescription = getAdminStateLabel(element.resumeToState)
  element.nextActionOwnerTypeDescription = element.nextActionOwnerType || '-'

  if (!element.id) {
    element.tempId = `${element.businessKey}-${index}`
  }

  return element
}
