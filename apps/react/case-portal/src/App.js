import { useEffect, useState, lazy, Suspense } from 'react'
import { ThemeRoutes } from './routes'
import ThemeCustomization from './themes'
import { SessionStoreProvider } from './SessionStoreContext'
import { CaseService, RecordService, MenuEventService } from 'services'
import menuItemsDefs from './menu'
import { RegisterInjectUserSession, RegisteOptions } from './plugins'
import { accountStore, sessionStore } from './store'
import RecordTypeChoice from './components/@formio/RecordTypeChoice'
import { Formio } from 'formiojs'
import {
  AdminLifecycleStages,
  getAdminWorkViewsByStage,
} from 'common/adminLifecycle'
import './App.css'

const ScrollTop = lazy(() => import('./components/ScrollTop'))

const App = () => {
  const isVisualPreview =
    process.env.NODE_ENV === 'development' &&
    new URLSearchParams(window.location.search).get('preview') === '1'

  const [keycloak, setKeycloak] = useState({})
  const [authenticated, setAuthenticated] = useState(null)
  const [recordsTypes, setRecordsTypes] = useState([])
  const [casesDefinitions, setCasesDefinitions] = useState([])
  const [menu, setMenu] = useState({ items: [] })

  useEffect(() => {
    if (isVisualPreview) {
      const previewKeycloak = {
        logout: () => undefined,
        idTokenParsed: {
          given_name: 'Preview',
          name: 'Levine Law Preview',
          email: 'preview@levinelaw.ca',
        },
        tokenParsed: {
          name: 'Levine Law Preview',
          email: 'preview@levinelaw.ca',
        },
        hasRealmRole: () => true,
        updateToken: () => Promise.resolve(false),
      }
      const previewMenu = {
        items: [...menuItemsDefs.items],
      }
      const previewCaseDefinitions = [
        { id: 'admin-opening', name: 'Matter Opening' },
        { id: 'corporate', name: 'Corporate Matters' },
      ]
      const previewRecordTypes = [{ id: 'clients' }, { id: 'companies' }]

      previewMenu.items[1].children
        .find((menu) => menu.id === 'case-list')
        .children.push(
          buildAdminWorkViewMenu(),
          {
            id: 'admin-opening',
            title: 'Matter Opening',
            type: 'item',
            url: '/case-list/admin-opening',
            breadcrumbs: true,
          },
          {
            id: 'corporate',
            title: 'Corporate Matters',
            type: 'item',
            url: '/case-list/corporate',
            breadcrumbs: true,
          },
        )
      previewMenu.items[1].children
        .find((menu) => menu.id === 'record-list')
        .children.push(
          {
            id: 'clients',
            title: 'Clients',
            type: 'item',
            url: '/record-list/clients',
            breadcrumbs: true,
          },
          {
            id: 'companies',
            title: 'Companies',
            type: 'item',
            url: '/record-list/companies',
            breadcrumbs: true,
          },
        )

      setKeycloak(previewKeycloak)
      setAuthenticated(true)
      setRecordsTypes(previewRecordTypes)
      setCasesDefinitions(previewCaseDefinitions)
      setMenu(previewMenu)
      return
    }

    const { keycloak } = sessionStore.bootstrap()

    keycloak.init({ onLoad: 'login-required' }).then((authenticated) => {
      setKeycloak(keycloak)
      setAuthenticated(authenticated)
      buildMenuItems(keycloak)
      RegisterInjectUserSession(keycloak)
      RegisteOptions(keycloak)
      forceLogoutIfUserNoMinimalRoleForSystem(keycloak)
      registerExtensionModulesFormio()

      const unsubscribe = MenuEventService.subscribeToMenuUpdates(() => {
        buildMenuItems(keycloak)
      })

      return () => {
        if (unsubscribe) unsubscribe()
      }
    })

    keycloak.onAuthRefreshError = () => {
      window.location.reload()
    }

    keycloak.onTokenExpired = () => {
      keycloak
        .updateToken(70)
        .then((refreshed) => {
          if (refreshed) {
            console.info('Token refreshed: ' + refreshed)
            RegisterInjectUserSession(keycloak)
            RegisteOptions(keycloak)
          } else {
            console.info(
              'Token not refreshed, valid for ' +
                Math.round(
                  keycloak.tokenParsed.exp +
                    keycloak.timeSkew -
                    new Date().getTime() / 1000,
                ) +
                ' seconds',
            )
          }
        })
        .catch(() => {
          console.error('Failed to refresh token')
        })
    }
  }, [isVisualPreview])

  function registerExtensionModulesFormio() {
    Formio.use(RecordTypeChoice)
  }

  async function forceLogoutIfUserNoMinimalRoleForSystem(keycloak) {
    if (!accountStore.hasAnyRole(keycloak)) {
      return keycloak.logout({ redirectUri: window.location.origin })
    }
  }

  function enableExternalLinkMenuItemIfRequired(menu) {
    if (!menu.items[0]?.children?.length) {
      delete menu.items[0]
    }
  }

  async function buildMenuItems(keycloak) {
    const menu = {
      items: [...menuItemsDefs.items],
    }

    if (menu.items[1].children) {
      const recordListMenu = menu.items[1].children.find(
        (menu) => menu.id === 'record-list',
      )
      if (recordListMenu) {
        recordListMenu.children = []
      }
    }

    await RecordService.getAllRecordTypes(keycloak).then((data) => {
      setRecordsTypes(data)

      data.forEach((element) => {
        menu.items[1].children
          .filter((menu) => menu.id === 'record-list')[0]
          .children.push({
            id: element.id,
            title: element.id,
            type: 'item',
            url: '/record-list/' + element.id,
            breadcrumbs: true,
          })
      })
    })

    if (menu.items[1].children) {
      const caseListMenu = menu.items[1].children.find(
        (menu) => menu.id === 'case-list',
      )
      if (caseListMenu) {
        caseListMenu.children = []
        caseListMenu.children.push(buildAdminWorkViewMenu())
      }
    }

    await CaseService.getCaseDefinitions(keycloak).then((data) => {
      setCasesDefinitions(data)

      data.forEach((element) => {
        menu.items[1].children
          .filter((menu) => menu.id === 'case-list')[0]
          .children.push({
            id: element.id,
            title: element.name,
            type: 'item',
            url: '/case-list/' + element.id,
            breadcrumbs: true,
          })
      })
    })

    if (!accountStore.isManagerUser(keycloak)) {
      delete menu.items[2]
    }

    enableExternalLinkMenuItemIfRequired(menu)

    return setMenu(menu)
  }

  function buildAdminWorkViewMenu() {
    return {
      id: 'admin-work-views',
      title: 'Admin Work Views',
      type: 'collapse',
      caption: 'Filtered queues under the five-stage lifecycle',
      children: AdminLifecycleStages.map((stage) => {
        const workViews = getAdminWorkViewsByStage(stage)

        if (!workViews.length) {
          return null
        }

        return {
          id: `admin-stage-${stage.toLowerCase()}`,
          title: stage,
          type: 'collapse',
          children: workViews.map((workView) => ({
            id: workView.id,
            title: workView.title,
            type: 'item',
            url: workView.url,
            breadcrumbs: true,
          })),
        }
      }).filter(Boolean),
    }
  }

  return (
    keycloak &&
    authenticated && (
      <ThemeCustomization>
        <Suspense fallback={<div>Loading...</div>}>
          <ScrollTop>
            <SessionStoreProvider value={{ keycloak, menu }}>
              <ThemeRoutes
                keycloak={keycloak}
                authenticated={authenticated}
                recordsTypes={recordsTypes}
                casesDefinitions={casesDefinitions}
              />
            </SessionStoreProvider>
          </ScrollTop>
        </Suspense>
      </ThemeCustomization>
    )
  )
}

export default App
