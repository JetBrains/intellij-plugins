import { createVaporApp } from 'vue'
import App from './App.vue'

import globalComponents from './global-components'

const app = createVaporApp(App)
  .use(globalComponents)
