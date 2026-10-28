import {PageTemplate} from '@stellar-enclosure/stellar-enclosure-web-shared'
import {Typography} from 'antd'

export const HomePage = () => <PageTemplate title="首页" pageState={{loading: false, error: null, empty: false}}>
  <Typography.Paragraph>欢迎使用 Stellar Enclosure。</Typography.Paragraph>
</PageTemplate>
