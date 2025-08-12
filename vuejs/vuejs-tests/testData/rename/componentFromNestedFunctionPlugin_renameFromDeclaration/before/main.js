import { createApp } from 'vue'
import App from './App.vue'

import globalComponents from './global-components'

const app = createApp(App)
  .use(globalComponents)
