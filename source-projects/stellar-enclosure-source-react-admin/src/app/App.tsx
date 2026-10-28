import {useRbac3Authorization, type FrontendNavigationNode} from '@stellar-enclosure/stellar-enclosure-tianquan-jianshen-react-sdk'
import {AppErrorBoundary, EnterpriseLayout, type EnterpriseNavigationItem} from '@stellar-enclosure/stellar-enclosure-web-shared'
import {QueryClient, QueryClientProvider} from '@tanstack/react-query'
import {Alert, Button, Result, Spin} from 'antd'
import {useState} from 'react'
import {BrowserRouter, Navigate, Route, Routes} from 'react-router-dom'
import {AuthorizationProvider} from './AuthorizationProvider'
import {auth} from './api'
import {RoleActivation} from './RoleActivation'
import {routes} from './routes'

const queryClient = new QueryClient({defaultOptions: {queries: {retry: false, refetchOnWindowFocus: false}, mutations: {retry: false}}})

const navigationItem = (node: FrontendNavigationNode): EnterpriseNavigationItem | null => {
  const children = node.children.map(navigationItem).filter((value): value is EnterpriseNavigationItem => value !== null)
  const path = typeof node.target.route === 'string' ? node.target.route : undefined
  if (node.type === 'MENU' && !path && children.length === 0) return null
  return {key: node.key, label: typeof node.target.label === 'string' ? node.target.label : node.key,
    path, children: children.length ? children : undefined}
}

const Console = ({embedded}: {readonly embedded: boolean}) => {
  const authorization = useRbac3Authorization()
  const [logoutError, setLogoutError] = useState<string>()
  if (['UNINITIALIZED', 'LOADING_ABOUT'].includes(authorization.status)) return <Spin fullscreen description="加载权限" />
  if (['ACTIVATION_REQUIRED', 'REPLACING_ACTIVE_ROLES'].includes(authorization.status)) return <RoleActivation />
  if (authorization.status === 'AUTHENTICATION_REQUIRED') return <Result title="请重新登录" extra={<Button onClick={() => auth.login()}>登录</Button>} />
  if (authorization.status !== 'READY' || !authorization.about) return <Result status="warning" title="授权状态不可用"
    subTitle={authorization.errorCode} extra={<Button onClick={() => void authorization.retry()}>重试</Button>} />
  const {resourceRegistry, snapshot, binding, about} = authorization
  const allowed = (key: string) => resourceRegistry.canAccessRoute(key, snapshot, binding)
  const landing = routes.find(route => allowed(route.resourceKey))?.path
  const navigation = resourceRegistry.navigation(snapshot, binding).map(navigationItem)
    .filter((value): value is EnterpriseNavigationItem => value !== null)
  return <EnterpriseLayout config={{platformName: 'Stellar Enclosure', navigation, hideHeader: embedded, hideFooter: embedded,
    user: {name: about.user.subject, menu: [{key: 'logout', label: '退出登录', onClick: () => {
      void auth.logout().catch(failure => setLogoutError(failure instanceof Error ? failure.message : '退出登录失败'))
    }}]}}}>
    {logoutError && <Alert type="warning" showIcon message={logoutError} />}
    <Routes>
      {routes.map(route => <Route key={route.resourceKey} path={route.path} element={allowed(route.resourceKey)
        ? <route.component /> : <Result status="403" title="无权访问此页面" />} />)}
      <Route path="*" element={landing ? <Navigate to={landing} replace /> : <Result status="403" title="没有可访问页面" />} />
    </Routes>
  </EnterpriseLayout>
}

export const App = ({embedded = false}: {readonly embedded?: boolean}) => <QueryClientProvider client={queryClient}>
  <BrowserRouter basename={import.meta.env.BASE_URL}>
    <AppErrorBoundary><AuthorizationProvider><Console embedded={embedded} /></AuthorizationProvider></AppErrorBoundary>
  </BrowserRouter>
</QueryClientProvider>
