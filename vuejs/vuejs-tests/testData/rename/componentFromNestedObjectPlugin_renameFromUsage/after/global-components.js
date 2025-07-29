import StandardButton from "./StandardButton.vue"

import otherGlobalComponents from './other-global-components'

const GlobalComponentsPlugin = {
  install(app) {
    app.component("MyButtonFromPlugin", StandardButton)

    app.use(otherGlobalComponents)
  }
}

export default GlobalComponentsPlugin
