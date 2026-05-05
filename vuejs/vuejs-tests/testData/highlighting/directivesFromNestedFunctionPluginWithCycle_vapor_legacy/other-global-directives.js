import StandardLabel from "./StandardLabel.vue"

import globalDirectives from './global-directives'
import { mutate } from "./directives/mutate"

export default function (app) {
  app.use(globalDirectives)

  app.component("MyLabelFromPlugin", StandardLabel)
  app.directive("my-mutate", mutate)
}
