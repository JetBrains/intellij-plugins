import StandardLabel from "./StandardLabel.vue"

import { mutate } from "./directives/mutate"

const OtherGlobalDirectivesPlugin = {
  install(app) {
    app.component("MyLabelFromPlugin", StandardLabel)
    app.directive("my-mutate", mutate)
  }
}

export default OtherGlobalDirectivesPlugin
