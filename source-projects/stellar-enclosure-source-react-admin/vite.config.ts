import {resolve} from 'node:path'
import {pathToFileURL} from 'node:url'
import react from '@vitejs/plugin-react'
import {egonFaviconPlugin} from '@stellar-enclosure/stellar-enclosure-web-shared/vite-plugin'
import {defineConfig} from 'vitest/config'
import {loadEnv} from 'vite'

export default defineConfig(async ({mode, command}) => {
  const base = loadEnv(mode, process.cwd(), 'VITE_').VITE_BASE_PATH ?? '/'
  if (!base.startsWith('/') || base.startsWith('//') || /[\\?#%]/.test(base)
    || /\/{2,}/.test(base) || base.split('/').some(segment => segment === '.' || segment === '..')
    || Array.from(base).some(character => character.charCodeAt(0) < 32 || character.charCodeAt(0) === 127)) {
    throw new Error('Invalid VITE_BASE_PATH')
  }
  const permissionPlugins: import('vite').PluginOption[] = []
  if (command === 'build') {
    if (!process.env.PAC_SDK_DIR) throw new Error('PAC_SDK_DIR_REQUIRED')
    const plugin = await import(pathToFileURL(resolve(process.env.PAC_SDK_DIR, 'scripts/permissions/consumer-vite-plugin.mjs')).href)
    permissionPlugins.push(plugin.permissionBuildPlugin())
  }
  return {
    base: base === '/' ? '/' : base.replace(/\/?$/, '/'),
    plugins: [react(), egonFaviconPlugin(), ...permissionPlugins],
    resolve: {dedupe: ['react', 'react-dom', 'antd', '@tanstack/react-query', 'react-router-dom', 'react-i18next', 'i18next', '@ant-design/icons']},
    server: {host: '127.0.0.1'},
    test: {environment: 'jsdom', fileParallelism: false, include: ['src/**/*.{test,spec}.{ts,tsx}']},
  }
})
