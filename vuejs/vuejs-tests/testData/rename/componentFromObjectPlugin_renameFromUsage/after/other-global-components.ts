import {App} from 'vue'
import StandardLabel from "./StandardLabel.vue"

const OtherGlobalComponentsPlugin = {
  install(app: App) {
    app.component("MyLabelFromPlugin", StandardLabel)
  }
}

export default OtherGlobalComponentsPlugin
