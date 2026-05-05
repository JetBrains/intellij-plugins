import {App} from 'vue'
import StandardButton from "./StandardButton.vue"

import { intersect } from "./directives/intersect"

const GlobalDirectivesPlugin = {
  install(app: App) {
    app.component("MyButtonFromPlugin", StandardButton)
    app.directive("my-intersect", intersect)
  }
}

export default GlobalDirectivesPlugin
