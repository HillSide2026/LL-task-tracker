import Config from '../consts'

const DEFAULT_API_BASE_URL = '/api'

export class ApiError extends Error {
  constructor(message, { status, body } = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.body = body
  }
}

export function getApiBaseUrl() {
  return stripTrailingSlash(Config.CaseEngineUrl || DEFAULT_API_BASE_URL)
}

export async function apiJson(keycloak, path, options = {}) {
  const resp = await apiFetch(keycloak, path, options)

  if (resp.status === 204) {
    return null
  }

  return resp.json()
}

export async function apiNoContent(keycloak, path, options = {}) {
  return apiFetch(keycloak, path, options)
}

export async function apiText(keycloak, path, options = {}) {
  const resp = await apiFetch(keycloak, path, options)
  return resp.text()
}

export async function apiFetch(keycloak, path, options = {}) {
  await refreshTokenIfNeeded(keycloak)

  const resp = await fetch(buildUrl(path, options.query), {
    method: options.method || 'GET',
    credentials: 'include',
    headers: buildHeaders(keycloak, options.headers),
    body: normalizeBody(options.body),
  })

  return handleResponse(keycloak, resp)
}

function buildUrl(path, query) {
  const baseUrl = getApiBaseUrl()
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  const queryString = buildQueryString(query)

  return `${baseUrl}${normalizedPath}${queryString}`
}

function buildQueryString(query) {
  if (!query) {
    return ''
  }

  const params = new URLSearchParams()

  Object.entries(query).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      params.append(key, value)
    }
  })

  const queryString = params.toString()
  return queryString ? `?${queryString}` : ''
}

function buildHeaders(keycloak, headers = {}) {
  const requestHeaders = {
    Accept: 'application/json',
    ...headers,
  }

  if (keycloak?.token) {
    requestHeaders.Authorization = `Bearer ${keycloak.token}`
  }

  return requestHeaders
}

function normalizeBody(body) {
  if (body === undefined || body === null) {
    return undefined
  }

  if (
    typeof body === 'string' ||
    (typeof FormData !== 'undefined' && body instanceof FormData)
  ) {
    return body
  }

  return JSON.stringify(body)
}

async function handleResponse(keycloak, resp) {
  if (resp.ok) {
    return resp
  }

  const body = await readErrorBody(resp)

  if (resp.status === 401) {
    logout(keycloak)
    throw new ApiError(body || 'Authentication required', {
      status: resp.status,
      body,
    })
  }

  if (resp.status === 403) {
    throw new ApiError(body || 'Permission denied', {
      status: resp.status,
      body,
    })
  }

  if (resp.status >= 500) {
    throw new ApiError(body || `Operational API error: ${resp.status}`, {
      status: resp.status,
      body,
    })
  }

  throw new ApiError(body || `API request failed: ${resp.status}`, {
    status: resp.status,
    body,
  })
}

async function readErrorBody(resp) {
  try {
    return await resp.text()
  } catch (e) {
    return ''
  }
}

async function refreshTokenIfNeeded(keycloak) {
  if (!keycloak?.token) {
    throw new ApiError('Authentication required', { status: 401 })
  }

  if (!keycloak.updateToken) {
    return
  }

  try {
    await keycloak.updateToken(30)
  } catch (e) {
    logout(keycloak)
    throw new ApiError('Authentication refresh failed', { status: 401 })
  }
}

function logout(keycloak) {
  const redirectUri =
    typeof window !== 'undefined' ? window.location.origin : undefined

  keycloak?.logout?.(redirectUri ? { redirectUri } : undefined)
}

function stripTrailingSlash(value) {
  return value.endsWith('/') ? value.slice(0, -1) : value
}
