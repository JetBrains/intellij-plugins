import StandardLabel from "./StandardLabel.vue"

import globalComponents from './global-components'

const OtherGlobalComponentsPlugin = {
  install(app) {
    app.use(globalComponents)

    app.component("MyLabelFromPlugin", StandardLabel)
  }
}

export default OtherGlobalComponentsPlugin
