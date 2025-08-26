import { createVaporApp } from 'vue'
import App from './App.vue'

import globalComponents from './global-components'
import otherGlobalComponents from './other-global-components'

const app = createVaporApp(App)
  .use(globalComponents)

app.use(otherGlobalComponents)