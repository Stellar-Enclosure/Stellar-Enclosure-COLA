import type {ComponentType} from 'react'
import {HomePage} from '../features/home/HomePage'

export interface FeatureRoute {
  readonly path: string
  readonly resourceKey: string
  readonly component: ComponentType
}

const featureModules = import.meta.glob<{routes: readonly FeatureRoute[]}>('../features/**/*.routes.tsx', {eager: true})
export const routes: readonly FeatureRoute[] = [
  {path: '/home', resourceKey: 'app.page.home', component: HomePage},
  ...Object.keys(featureModules).sort().flatMap(key => featureModules[key].routes),
]
