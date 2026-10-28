import {defineMessages, defineResources} from '@stellar-enclosure/stellar-enclosure-web-shared/permissions'

export const applicationResources = defineResources([
  {key: 'app.app.main', type: 'APP', target: {applicationCode: 'stellar-enclosure-source-react-admin'}, access: 'PUBLIC',
    i18n: {pageCode: 'app.web', labelKey: 'app.title'}},
  {key: 'app.page.home', type: 'PAGE', target: {route: '/home', label: '首页', order: '1'}, access: 'PUBLIC',
    i18n: {pageCode: 'app.web', labelKey: 'home.title'}},
] as const)

export const applicationMessages = defineMessages([
  {pageCode: 'app.web', key: 'app.title', defaultLocale: 'zh-CN', defaults: {'zh-CN': 'Stellar Enclosure'}},
  {pageCode: 'app.web', key: 'home.title', defaultLocale: 'zh-CN', defaults: {'zh-CN': '首页'}},
] as const)
