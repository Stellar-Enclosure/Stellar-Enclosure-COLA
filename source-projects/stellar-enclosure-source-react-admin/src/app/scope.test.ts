import {describe, expect, it} from 'vitest'
import type {GatewayIdentityResult} from '@stellar-enclosure/stellar-enclosure-web-shared'
import type {PermissionSnapshot} from '@stellar-enclosure/stellar-enclosure-web-shared/permissions'
import {matchesCurrentScope} from './scope'

const identity: GatewayIdentityResult = {
  issuer: 'https://identity.example/tenants/42', tenantId: '42', subject: 'user', appAlias: 'edge-example', clientId: 'client',
  authorizationExpiresAt: 2_000_000_000, authenticationExpiresAt: 2_000_000_000,
  securityVersion: {realm: '1', client: '1', subject: '1'}, ssoLogoutUri: 'https://identity.example/logout',
}
const snapshot: PermissionSnapshot = {
  issuer: identity.issuer, subject: identity.subject,
  catalogScope: {catalogScopeId: '101', tenantId: '42', businessCode: 'example', applicationCode: 'orders', namespaceCode: 'default', environment: 'dev'},
  catalogVersion: '1', authVersion: '1', policyVersion: '1',
  activeRoleContext: {applicationCode: 'orders', rootRoleIds: ['1'], effectiveRoleIds: ['1']},
  permissionCodes: ['orders:read'], snapshotExpiresAt: '2030-01-01T00:00:00Z',
}
const scope = {businessCode: 'example', applicationCode: 'orders', namespaceCode: 'default', environment: 'dev'}
const now = Date.parse('2026-01-01T00:00:00Z')

describe('matchesCurrentScope', () => {
  it('accepts only the current identity and deployment scope', () => {
    expect(matchesCurrentScope(identity, snapshot, scope, now)).toBe(true)
    expect(matchesCurrentScope({...identity, tenantId: '43'}, snapshot, scope, now)).toBe(false)
    expect(matchesCurrentScope({...identity, subject: 'other'}, snapshot, scope, now)).toBe(false)
    expect(matchesCurrentScope(identity, snapshot, {...scope, namespaceCode: 'other'}, now)).toBe(false)
    expect(matchesCurrentScope(identity, snapshot, {...scope, environment: ''}, now)).toBe(false)
  })

  it('rejects expired snapshots', () => {
    expect(matchesCurrentScope(identity, snapshot, scope, Date.parse('2031-01-01T00:00:00Z'))).toBe(false)
  })
})
