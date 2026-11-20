import {Rbac3Provider} from '@stellar-enclosure/stellar-enclosure-tianquan-jianshen-react-sdk'
import {loadFieldCatalog, snapshotContext, type BackendFieldCatalog, type PermissionSnapshotContext, type ResourceReference} from '@stellar-enclosure/stellar-enclosure-web-shared/permissions'
import {Alert, Button, Result, Spin} from 'antd'
import {type PropsWithChildren, useCallback, useEffect, useRef, useState} from 'react'
import manifest from '../../permission-manifest.config.json'
import {generatedReferences, generatedResources} from '../generated/permissions'
import {applicationResources} from '../permissions/resources'
import {type ApplicationAuthorization, readAuthorizationEnvelope} from './api'
import {matchesCurrentScope} from './scope'

const resources = [...applicationResources, ...generatedResources]
const references: readonly ResourceReference[] = generatedReferences
const fieldKeys = [...new Set(references.filter(value =>
  ['FORM_FIELD', 'TABLE_FIELD', 'DETAIL_FIELD'].includes(value.usage)).map(value => value.key))]

export const AuthorizationProvider = ({children, auth}: PropsWithChildren<{readonly auth: ApplicationAuthorization}>) => {
  const {api, authorizationClient, context} = auth
  const [binding, setBinding] = useState<PermissionSnapshotContext | null>(null)
  const [catalog, setCatalog] = useState<BackendFieldCatalog | null>(null)
  const [catalogError, setCatalogError] = useState(false)
  const [phase, setPhase] = useState<'LOADING' | 'READY' | 'LOGIN' | 'FORBIDDEN' | 'FAILED'>('LOADING')
  const [error, setError] = useState<string>()
  const pending = useRef<AbortController | null>(null)
  const serial = useRef(0)
  const acceptedEpoch = useRef<number | null>(null)

  const clear = useCallback((next: 'LOGIN' | 'FORBIDDEN') => {
    serial.current += 1
    pending.current?.abort()
    acceptedEpoch.current = null
    setBinding(null)
    setCatalog(null)
    setPhase(next)
  }, [])

  const load = useCallback(async () => {
    pending.current?.abort()
    const controller = new AbortController()
    pending.current = controller
    const attempt = ++serial.current
    setBinding(null)
    setCatalog(null)
    setPhase('LOADING')
    try {
      const identity = auth.identity()
      if (!identity) {
        setPhase('LOGIN')
        return
      }
      const capture = context.capture(controller.signal)
      const snapshot = await authorizationClient.getPermissionSnapshot(capture.signal)
      capture.assertCurrent()
      if (attempt !== serial.current) return
      if (!matchesCurrentScope(identity, snapshot, {
        ...manifest.application,
        namespaceCode: import.meta.env.VITE_PERMISSION_NAMESPACE ?? '',
        environment: import.meta.env.VITE_PERMISSION_ENVIRONMENT ?? '',
      })) throw new Error('权限快照与当前身份或部署范围不一致')
      acceptedEpoch.current = capture.epoch
      setBinding(snapshotContext(snapshot, capture.epoch))
      setPhase('READY')
    } catch (failure) {
      if (attempt !== serial.current) return
      const status = (failure as {status?: number}).status
      setPhase(status === 401 ? 'LOGIN' : status === 403 ? 'FORBIDDEN' : 'FAILED')
      setError(failure instanceof Error ? failure.message : '授权服务暂不可用')
    }
  }, [auth, authorizationClient, context])

  useEffect(() => {
    const unsubscribeFailure = auth.subscribeAuthorizationFailure(status => clear(status === 401 ? 'LOGIN' : 'FORBIDDEN'))
    const unsubscribeEpoch = context.subscribe(() => {
      if (acceptedEpoch.current !== null && acceptedEpoch.current !== context.epoch) clear('LOGIN')
    })
    let active = true
    queueMicrotask(() => { if (active) void load() })
    return () => { active = false; pending.current?.abort(); serial.current += 1; unsubscribeFailure(); unsubscribeEpoch() }
  }, [auth, clear, context, load])

  useEffect(() => {
    if (!binding || fieldKeys.length === 0) return
    const controller = new AbortController()
    const capture = context.capture(controller.signal)
    setCatalog(null)
    setCatalogError(false)
    void loadFieldCatalog(fieldKeys, async query => {
      const response = await api.request<unknown>('/api/v1/authorization/field-catalog?' + query.toString(), {signal: capture.signal})
      capture.assertCurrent()
      return readAuthorizationEnvelope<BackendFieldCatalog>(response)
    }).then(value => { capture.assertCurrent(); setCatalog(value) })
      .catch(failure => { if (!(failure instanceof DOMException && failure.name === 'AbortError')) setCatalogError(true) })
    return () => controller.abort()
  }, [api, binding, context])

  if (phase === 'LOADING') return <Spin fullscreen description="加载当前身份与权限范围" />
  if (phase === 'LOGIN') return <Result title="请登录" subTitle={error}
    extra={auth.requiresStandaloneLogin
      ? <Button type="primary" href={auth.standaloneUrl} target="_blank" rel="noopener noreferrer">打开应用登录</Button>
      : <Button type="primary" onClick={() => {
        setError(undefined)
        void auth.login().then(load).catch(() => setError('登录未完成，请重试'))
      }}>统一身份登录</Button>} />
  if (phase !== 'READY') return <Result status={phase === 'FORBIDDEN' ? '403' : 'warning'}
    title={phase === 'FORBIDDEN' ? '当前身份无权访问' : '授权服务暂不可用'} subTitle={error}
    extra={<Button onClick={() => void load()}>重新加载</Button>} />

  return <Rbac3Provider client={authorizationClient} binding={binding} requestContext={context}
    resources={resources} resourceReferences={references} fieldCatalog={catalog}
    onContextChange={setBinding}>
    {catalogError && <Alert type="warning" showIcon message="字段权限暂不可用，相关字段已隐藏或禁用" />}
    {children}
  </Rbac3Provider>
}
