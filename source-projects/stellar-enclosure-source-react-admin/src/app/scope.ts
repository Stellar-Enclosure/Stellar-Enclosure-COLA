import type {GatewayIdentityResult} from '@stellar-enclosure/stellar-enclosure-web-shared'
import {isPermissionSnapshot, snapshotExpiry, type PermissionSnapshot} from '@stellar-enclosure/stellar-enclosure-web-shared/permissions'

export interface ExpectedScope {
  readonly businessCode: string
  readonly applicationCode: string
  readonly namespaceCode: string
  readonly environment: string
}

export const matchesCurrentScope = (identity: GatewayIdentityResult, snapshot: PermissionSnapshot,
  expected: ExpectedScope, now = Date.now()): boolean => isPermissionSnapshot(snapshot)
  && snapshot.issuer === identity.issuer && snapshot.subject === identity.subject
  && snapshot.catalogScope.tenantId === identity.tenantId
  && expected.businessCode.length > 0 && snapshot.catalogScope.businessCode === expected.businessCode
  && expected.applicationCode.length > 0 && snapshot.catalogScope.applicationCode === expected.applicationCode
  && expected.namespaceCode.length > 0 && snapshot.catalogScope.namespaceCode === expected.namespaceCode
  && expected.environment.length > 0 && snapshot.catalogScope.environment === expected.environment
  && snapshotExpiry(snapshot.snapshotExpiresAt) > now
