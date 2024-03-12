declare module "*.vue" {
  import Vue from 'vue'
  export default Vue
}

import ButtonSFC from "./src/components/ButtonSFC.vue"

declare module 'vue' {
  export interface GlobalComponents {
    ButtonSFC: typeof ButtonSFC
  }
}
