import {createApp} from "vue"

import TheGlobalComponent from "./TheGlobalComponent.vue"

createApp()
  .component("Foo", TheGlobalComponent)