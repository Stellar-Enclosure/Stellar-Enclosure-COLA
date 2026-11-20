import {
  createHttpClient, createPublicOAuthClient, PublicOAuthError, relayPublicOAuthPopupCode,
} from '@stellar-enclosure/stellar-enclosure-web-shared'
import {Rbac3ApiClient} from '@stellar-enclosure/stellar-enclosure-tianquan-jianshen-react-sdk'

type PublicEnvironment = Readonly<Record<string, string | boolean | undefined>>
type Browser = Pick<Window, 'location' | 'origin' | 'history' | 'open' | 'addEventListener' | 'removeEventListener'>

const registrationId = 'react-admin'

export const createApplicationAuthorization = (env: PublicEnvironment, embedded: boolean,
  browser: Browser = window, fetcher?: typeof fetch) => {
  const required = (key: string) => {
    const value = env[key]
    if (typeof value !== 'string' || value.length === 0) throw new PublicOAuthError('INVALID_CONFIGURATION')
    return value
  }
  const webOrigin = required('VITE_OAUTH_WEB_ORIGIN')
  const origin = new URL(webOrigin)
  const base = required('BASE_URL')
  if (origin.origin !== webOrigin || origin.protocol !== 'https:'
    || !base.startsWith('/') || !base.endsWith('/') || base.startsWith('//')
    || new URL(base, webOrigin).href !== webOrigin + base
    || (!embedded && (browser.location.origin !== webOrigin || browser.origin !== webOrigin))) {
    throw new PublicOAuthError('INVALID_CONFIGURATION')
  }

  const callbackPath = base + 'oauth/callback'
  const popupPath = base + 'oauth/popup-callback'
  const redirectUri = webOrigin + callbackPath
  const popupRedirectUri = webOrigin + popupPath
  const allowedReturnPath = (path: string) => path.startsWith(base) && !path.startsWith('//')
    && /^[\/A-Za-z0-9._~-]{1,2048}$/.test(path)
    && !path.split('/').some(segment => segment === '.' || segment === '..')
    && path !== callbackPath && path !== popupPath
  const replaceLocation = (url: string) => browser.history.replaceState(browser.history.state, '', url)
  const client = createPublicOAuthClient({
    registrations: [{
      id: registrationId,
      tenantId: required('VITE_OAUTH_TENANT_ID'),
      issuer: required('VITE_OAUTH_ISSUER'),
      clientId: required('VITE_OAUTH_CLIENT_ID'),
      resource: required('VITE_OAUTH_RESOURCE'),
      apiBaseUrl: required('VITE_OAUTH_API_BASE_URL'),
      scopes: required('VITE_OAUTH_SCOPES').trim().split(/\s+/),
      authorizationEndpoint: required('VITE_OAUTH_AUTHORIZATION_ENDPOINT'),
      tokenEndpoint: required('VITE_OAUTH_TOKEN_ENDPOINT'),
      jwksUri: required('VITE_OAUTH_JWKS_URI'),
      redirectUri,
      popupRedirectUri,
      browserBound: true,
    }],
    activeResource: registrationId,
    browserWindow: browser,
    fetch: fetcher,
    replaceLocation,
  })
  const authority = client.authority(registrationId)
  const listeners = new Set<(status: 401 | 403) => void>()
  const api = createHttpClient({
    baseUrl: authority.apiBaseUrl,
    authority,
    fetch: fetcher,
    onAuthError: status => listeners.forEach(listener => listener(status)),
  })
  let completion: Promise<'READY' | 'POPUP'> | undefined
  const finishCallback = async (): Promise<'READY' | 'POPUP'> => {
    const path = browser.location.pathname
    if (path !== callbackPath && path !== popupPath) return 'READY'
    if (embedded) throw new PublicOAuthError('INVALID_CALLBACK')
    if (path === popupPath) {
      relayPublicOAuthPopupCode(popupRedirectUri, browser as Window)
      return 'POPUP'
    }
    const {returnTo} = await client.completeRedirect(browser.location.href)
    if (!allowedReturnPath(returnTo)) {
      client.logout()
      throw new PublicOAuthError('INVALID_CALLBACK')
    }
    replaceLocation(returnTo)
    return 'READY'
  }
  return {
    api,
    context: authority.context,
    authorizationClient: new Rbac3ApiClient({transport: api}),
    identity: () => client.identity(registrationId),
    publicHomeUrl: webOrigin + base,
    standaloneUrl: embedded ? webOrigin + base : undefined,
    requiresStandaloneLogin: embedded && browser.origin !== webOrigin,
    completeLogin: () => completion ??= finishCallback(),
    login: async (reauthenticate = false) => {
      if (embedded) {
        if (browser.origin !== webOrigin || reauthenticate) throw new PublicOAuthError('POPUP_UNAVAILABLE')
        await client.authorizeInPopup(registrationId)
        return
      }
      const path = browser.location.pathname
      await client.login(allowedReturnPath(path) ? path : base, {reauthenticate})
    },
    logout: () => client.logout(),
    subscribeAuthorizationFailure: (listener: (status: 401 | 403) => void) => {
      listeners.add(listener)
      return () => { listeners.delete(listener) }
    },
  }
}

export type ApplicationAuthorization = ReturnType<typeof createApplicationAuthorization>

export const readAuthorizationEnvelope = <T,>(value: unknown): T => {
  if (!value || typeof value !== 'object' || !('success' in value) || value.success !== true
    || !('data' in value) || value.data === null || value.data === undefined) {
    throw new Error('Authorization response envelope is invalid')
  }
  return value.data as T
}
