import { createApp } from 'vue'
import App from './App.vue'

import globalDirectives from './global-directives'
import { clickOutside } from './directives/click-outside'

const app = createApp(App)
  .use(globalDirectives)
  .directive('my-click-outside', clickOutside)
