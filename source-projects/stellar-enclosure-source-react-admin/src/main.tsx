import {StrictMode} from 'react'
import {createRoot, type Root} from 'react-dom/client'
import 'antd/dist/reset.css'
import {AdminThemeProvider, I18nProvider, initI18n, injectTokens} from '@stellar-enclosure/stellar-enclosure-web-shared'
import {App} from './app/App'

injectTokens()
initI18n({defaultNS: 'common', resources: {'zh-CN': {}}})

interface EmbeddedWindow extends Window {
  __POWERED_BY_WUJIE__?: boolean
  __WUJIE_MOUNT?: () => void
  __WUJIE_UNMOUNT?: () => void
  __WUJIE?: {mount?: () => void}
  $wujie?: {props?: {embedded?: boolean}}
}

const runtime = window as EmbeddedWindow
let root: Root | undefined
const mount = () => {
  if (root) return
  const element = document.getElementById('root')
  if (!element) throw new Error('Application root is missing')
  root = createRoot(element)
  root.render(<StrictMode><I18nProvider><AdminThemeProvider>
    <App embedded={runtime.$wujie?.props?.embedded === true} />
  </AdminThemeProvider></I18nProvider></StrictMode>)
}
const unmount = () => { root?.unmount(); root = undefined }
if (runtime.__POWERED_BY_WUJIE__) {
  runtime.__WUJIE_MOUNT = mount
  runtime.__WUJIE_UNMOUNT = unmount
  runtime.__WUJIE?.mount?.()
} else mount()
