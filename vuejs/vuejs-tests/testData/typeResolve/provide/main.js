import {createApp, ref} from 'vue'
import App from './App.vue'

let app = createApp(App)

app.provide('globalProvide', 123)
app.provide('globalProvideRef', ref(123))

app.mount('#app')
