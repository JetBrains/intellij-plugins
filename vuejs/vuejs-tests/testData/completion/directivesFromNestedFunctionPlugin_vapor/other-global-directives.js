import StandardLabel from "./StandardLabel.vue"

import { mutate } from "./directives/mutate"

export default function (app) {
  app.component("MyLabelFromPlugin", StandardLabel)
  app.directive("my-mutate", mutate)
}
