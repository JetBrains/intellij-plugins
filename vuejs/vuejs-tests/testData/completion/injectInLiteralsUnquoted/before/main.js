import {createApp} from 'vue'
import App from './App.vue'

let app = createApp(App)

app.provide('provideGlobal', 123)

app.mount('#app')
