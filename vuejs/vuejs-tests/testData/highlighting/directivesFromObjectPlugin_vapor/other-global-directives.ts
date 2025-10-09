import {App} from 'vue'
import StandardLabel from "./StandardLabel.vue"

import { mutate } from "./directives/mutate"

const OtherGlobalDirectivesPlugin = {
  install(app: App) {
    app.component("MyLabelFromPlugin", StandardLabel)
    app.directive("my-mutate", mutate)
  }
}

export default OtherGlobalDirectivesPlugin
