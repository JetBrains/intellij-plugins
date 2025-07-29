import {App} from 'vue'
import StandardButton from "./StandardButton.vue"

const GlobalComponentsPlugin = {
  install(app: App) {
    app.component("MyButton<caret>FromPlugin", StandardButton)
  }
}

export default GlobalComponentsPlugin
