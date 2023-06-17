import { createApp } from 'vue'
import App from './App.vue'
import {myInjectionKey} from "./injectionKey";

let app = createApp(App)

app.provide(myInjectionKey, {
  name: 'bob'
})

app.mount('#app')
