import {beforeEach, describe, expect, it, vi} from 'vitest'
import {AuthRequestContext, createPublicOAuthClient, relayPublicOAuthPopupCode} from '@stellar-enclosure/stellar-enclosure-web-shared'
import {createApplicationAuthorization} from './api'

vi.mock('@stellar-enclosure/stellar-enclosure-web-shared', async importOriginal => ({
  ...await importOriginal<typeof import('@stellar-enclosure/stellar-enclosure-web-shared')>(),
  createPublicOAuthClient: vi.fn(),
  relayPublicOAuthPopupCode: vi.fn(),
}))

const webOrigin = 'https://console.example.test'
const apiBase = 'https://api.example.test/orders/'
const issuer = 'https://auth.example.test/tenants/42'
const env = {
  BASE_URL: '/admin/',
  VITE_OAUTH_WEB_ORIGIN: webOrigin,
  VITE_OAUTH_TENANT_ID: '42',
  VITE_OAUTH_ISSUER: issuer,
  VITE_OAUTH_CLIENT_ID: 'orders-admin',
  VITE_OAUTH_RESOURCE: 'https://resource.example.test/orders',
  VITE_OAUTH_API_BASE_URL: apiBase,
  VITE_OAUTH_SCOPES: 'openid orders.read',
  VITE_OAUTH_AUTHORIZATION_ENDPOINT: issuer + '/authorize',
  VITE_OAUTH_TOKEN_ENDPOINT: issuer + '/token',
  VITE_OAUTH_JWKS_URI: issuer + '/jwks',
}

const runtime = (path = '/admin/orders', nativeOrigin = webOrigin) => ({
  location: new URL(webOrigin + path) as unknown as Location,
  origin: nativeOrigin,
  history: {state: null, replaceState: vi.fn()} as unknown as History,
  open: vi.fn(), addEventListener: vi.fn(), removeEventListener: vi.fn(),
})

const login = vi.fn()
const completeRedirect = vi.fn()
const authorizeInPopup = vi.fn()
const logout = vi.fn()
const invalidate = vi.fn()

beforeEach(() => {
  vi.restoreAllMocks()
  vi.clearAllMocks()
  vi.mocked(createPublicOAuthClient).mockReturnValue({
    login, completeRedirect, authorizeInPopup, logout, identity: vi.fn(), selectResource: vi.fn(), bootstrap: vi.fn(),
    authority: () => ({apiBaseUrl: apiBase, context: new AuthRequestContext(), resource: env.VITE_OAUTH_RESOURCE,
      authorizationHeader: () => 'Bearer fixture-access', invalidate}),
  })
})

describe('React Admin OAuth', () => {
  it('binds direct API requests to the assigned Resource and omits cookies', async () => {
    const fetcher = vi.fn().mockResolvedValue(Response.json({data: 'ok'}))
    const app = createApplicationAuthorization(env, false, runtime(), fetcher)
    expect(vi.mocked(createPublicOAuthClient).mock.calls[0][0].registrations).toEqual([
      expect.objectContaining({resource: env.VITE_OAUTH_RESOURCE, apiBaseUrl: apiBase,
        redirectUri: webOrigin + '/admin/oauth/callback',
        popupRedirectUri: webOrigin + '/admin/oauth/popup-callback', browserBound: true}),
    ])
    await expect(app.api.request('/api/v1/orders')).resolves.toEqual({data: 'ok'})
    const [url, request] = fetcher.mock.calls[0]
    expect(url.toString()).toBe(apiBase + 'api/v1/orders')
    expect(request.credentials).toBe('omit')
    expect(request.headers.get('Authorization')).toBe('Bearer fixture-access')
  })

  it('completes the exact callback once and restores the route with no page reload', async () => {
    const browser = runtime('/admin/oauth/callback?code=fixture&state=fixture')
    completeRedirect.mockResolvedValue({returnTo: '/admin/orders'})
    const app = createApplicationAuthorization(env, false, browser)
    expect(app.completeLogin()).toBe(app.completeLogin())
    await app.completeLogin()
    expect(completeRedirect).toHaveBeenCalledWith(browser.location.href)
    expect(completeRedirect).toHaveBeenCalledTimes(1)
    expect(browser.history.replaceState).toHaveBeenCalledWith(null, '', '/admin/orders')
    await expect(app.login(true)).resolves.toBeUndefined()
    expect(login).toHaveBeenCalledWith('/admin/', {reauthenticate: true})
  })

  it('rejects an out-of-base return path and denies callback on a foreign or embedded origin', async () => {
    completeRedirect.mockResolvedValue({returnTo: '/admin-other/orders'})
    const callback = '/admin/oauth/callback?code=fixture'
    await expect(createApplicationAuthorization(env, false, runtime(callback)).completeLogin()).rejects.toThrow('INVALID_CALLBACK')
    expect(logout).toHaveBeenCalledOnce()
    expect(() => createApplicationAuthorization(env, false, runtime(callback, 'https://other.example.test'))).toThrow('INVALID_CONFIGURATION')
    await expect(createApplicationAuthorization(env, true, runtime(callback)).completeLogin()).rejects.toThrow('INVALID_CALLBACK')
  })

  it('uses the SDK popup relay and requires a matching native opener for embedded login', async () => {
    const browser = runtime('/admin/oauth/popup-callback?code=fixture')
    await expect(createApplicationAuthorization(env, false, browser).completeLogin()).resolves.toBe('POPUP')
    expect(relayPublicOAuthPopupCode).toHaveBeenCalledWith(webOrigin + '/admin/oauth/popup-callback', browser)
    await expect(createApplicationAuthorization(env, true, runtime()).login()).resolves.toBeUndefined()
    expect(authorizeInPopup).toHaveBeenCalledWith('react-admin')
    const foreign = createApplicationAuthorization(env, true, runtime('/admin/orders', 'https://portal.example.test'))
    expect(foreign.standaloneUrl).toBe(webOrigin + '/admin/')
    await expect(foreign.login()).rejects.toThrow('POPUP_UNAVAILABLE')
  })
})
