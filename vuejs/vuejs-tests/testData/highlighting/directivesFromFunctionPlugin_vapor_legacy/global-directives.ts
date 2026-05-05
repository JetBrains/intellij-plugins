import {App} from 'vue'
import StandardButton from "./StandardButton.vue"
import { intersect } from "./directives/intersect"

export default function (app: App) {
  app.component("MyButtonFromPlugin", StandardButton)
  app.directive("my-intersect", intersect)
}
