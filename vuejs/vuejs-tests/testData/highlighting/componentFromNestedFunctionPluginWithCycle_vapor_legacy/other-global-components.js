import StandardLabel from "./StandardLabel.vue"

import globalComponents from './global-components'

export default function (app) {
  app.use(globalComponents)

  app.component("MyLabelFromPlugin", StandardLabel)
}
