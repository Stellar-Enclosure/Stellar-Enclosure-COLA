import {createGatewayAuthClient, createHttpClient} from '@stellar-enclosure/stellar-enclosure-web-shared'
import {Rbac3ApiClient} from '@stellar-enclosure/stellar-enclosure-tianquan-jianshen-react-sdk'

export const auth = createGatewayAuthClient({
  baseUrl: import.meta.env.VITE_YUHENG_ORIGIN ?? '',
  appAlias: import.meta.env.VITE_OAUTH_APP_ALIAS ?? 'edge-example',
})

const listeners = new Set<(status: 401 | 403) => void>()
export const subscribeAuthorizationFailure = (listener: (status: 401 | 403) => void) => {
  listeners.add(listener)
  return () => { listeners.delete(listener) }
}

export const api = createHttpClient({
  baseUrl: auth.applicationBaseUrl,
  credentials: 'include',
  context: auth.context,
  getCsrf: signal => auth.csrf(signal),
  onAuthError: status => listeners.forEach(listener => listener(status)),
})

export const authorizationClient = new Rbac3ApiClient({transport: api})

export const readAuthorizationEnvelope = <T,>(value: unknown): T => {
  if (!value || typeof value !== 'object' || !('success' in value) || value.success !== true
    || !('data' in value) || value.data === null || value.data === undefined) {
    throw new Error('Authorization response envelope is invalid')
  }
  return value.data as T
}
