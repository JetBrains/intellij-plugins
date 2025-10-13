import StandardButton from "./StandardButton.vue"

import otherGlobalDirectives from './other-global-directives'
import { intersect } from "./directives/intersect"

const GlobalDirectivesPlugin = {
  install(app) {
    app.component("MyButtonFromPlugin", StandardButton)
    app.directive("my-intersect", intersect)

    app.use(otherGlobalDirectives)
  }
}

export default GlobalDirectivesPlugin
