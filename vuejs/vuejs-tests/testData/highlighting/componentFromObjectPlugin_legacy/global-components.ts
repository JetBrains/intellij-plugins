import {App} from 'vue'
import StandardButton from "./StandardButton.vue"

const GlobalComponentsPlugin = {
  install(app: App) {
    app.component("MyButtonFromPlugin", StandardButton)
  }
}

export default GlobalComponentsPlugin
