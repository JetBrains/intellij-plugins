import {App} from 'vue'
import StandardButton from "./StandardButton.vue"

export default function (app: App) {
  app.component("OtherButtonFromPlugin", StandardButton)
}
