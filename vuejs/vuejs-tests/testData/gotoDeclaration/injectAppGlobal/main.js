import {createApp} from 'vue'
import App from './App.vue'

let app = createApp(App)

app.provide('globalProvide', 123)

app.mount('#app')