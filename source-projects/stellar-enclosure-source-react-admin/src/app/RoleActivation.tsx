import {useRbac3Authorization} from '@stellar-enclosure/stellar-enclosure-tianquan-jianshen-react-sdk'
import {Alert, Button, Card, Checkbox, Space, Typography} from 'antd'
import {useState} from 'react'
import {auth} from './api'

export const RoleActivation = () => {
  const authorization = useRbac3Authorization()
  const initial = authorization.activeRoles?.activeRoles.flatMap(value => value.rootRoleIds) ?? []
  const [selection, setSelection] = useState<readonly string[] | null>(null)
  const [error, setError] = useState<string>()
  const selected = selection ?? initial
  const hasConflict = authorization.candidates?.applications.some(application => {
    const sets = new Set<string>()
    return application.candidates.filter(candidate => selected.includes(candidate.rootRoleId)).some(candidate =>
      candidate.mutexSetIds.some(id => { if (sets.has(id)) return true; sets.add(id); return false }))
  }) ?? false
  const submit = async () => {
    if (!authorization.activeRoles) return
    setError(undefined)
    try {
      await authorization.replaceActiveRoles({roleIds: selected, expectedAuthVersion: authorization.activeRoles.authVersion})
    } catch (failure) {
      setError(failure instanceof Error ? failure.message : '角色激活失败')
    }
  }
  return <Card title="激活角色">
    <Typography.Paragraph type="secondary">选择需要激活的角色后提交完整角色集合。</Typography.Paragraph>
    {(authorization.candidates?.configurationErrors.length ?? 0) > 0 &&
      <Alert type="error" showIcon message="角色配置存在错误" />}
    {hasConflict && <Alert type="error" showIcon message="所选角色在同一应用内互斥" />}
    {authorization.candidates?.applications.map(application => <Card size="small" key={application.applicationId} title={application.applicationCode}>
      <Space orientation="vertical">{application.candidates.map(candidate => <Checkbox key={candidate.rootRoleId}
        checked={selected.includes(candidate.rootRoleId)} onChange={event => setSelection(event.target.checked
          ? [...selected, candidate.rootRoleId] : selected.filter(id => id !== candidate.rootRoleId))}>
        {candidate.displayName}
      </Checkbox>)}</Space>
    </Card>)}
    {error && <Alert type="error" showIcon message={error} />}
    {authorization.errorCode === 'STEP_UP_REQUIRED' && <Button onClick={() => auth.login(undefined, {reauthenticate: true})}>重新认证</Button>}
    <Button type="primary" disabled={!authorization.activeRoles || hasConflict || (authorization.candidates?.configurationErrors.length ?? 0) > 0}
      loading={authorization.status === 'REPLACING_ACTIVE_ROLES'} onClick={() => void submit()}>激活所选角色</Button>
  </Card>
}
