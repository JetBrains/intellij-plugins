import {App} from 'vue'
import StandardLabel from "./StandardLabel.vue"

export default function (app: App) {
  app.component("MyLabelFromPlugin", StandardLabel)
}
