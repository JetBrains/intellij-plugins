import StandardButton from "./StandardButton.vue"

import otherGlobalComponents from './other-global-components'

export default function (app) {
  app.component("MyButtonFromPlugin", StandardButton)

  app.use(otherGlobalComponents)
}
