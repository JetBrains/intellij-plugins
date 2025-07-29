import {App} from 'vue'
import StandardButton from "./StandardButton.vue"

const GlobalComponentsPlugin = {
  install(app: App) {
    app.component("OtherButtonFromPlugin", StandardButton)
  }
}

export default GlobalComponentsPlugin
