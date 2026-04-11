/* eslint-disable no-undef */
import store from './index'

jest.mock('keycloak-js')

test('should initialize realm with subdomain when using dns', () => {
  window.location.assign('https://matters.levinellp.ca/')

  const { keycloak, realm, clientId } = store.bootstrap()

  expect(keycloak).not.toBeNull()
  expect(realm).toEqual('matters')
  expect(clientId).toEqual('wks-portal')
})

test('should initialize default realm when using localhost', () => {
  window.location.assign('http://localhost:3001/')

  const { keycloak, realm, clientId } = store.bootstrap()

  expect(keycloak).not.toBeNull()
  expect(realm).toEqual('localhost')
  expect(clientId).toEqual('wks-portal')
})

test('should initialize realm from non-production Levine dns', () => {
  window.location.assign('https://intake.levinellp.ca/')

  const { keycloak, realm, clientId } = store.bootstrap()

  expect(keycloak).not.toBeNull()
  expect(realm).toEqual('intake')
  expect(clientId).toEqual('wks-portal')
})
/* eslint-disable no-undef */
