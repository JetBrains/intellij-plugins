import StandardButton from "./StandardButton.vue"

import otherGlobalDirectives from './other-global-directives'
import { intersect } from "./directives/intersect"

export default function (app) {
  app.component("MyButtonFromPlugin", StandardButton)
  app.directive("my-intersect", intersect)

  app.use(otherGlobalDirectives)
}
