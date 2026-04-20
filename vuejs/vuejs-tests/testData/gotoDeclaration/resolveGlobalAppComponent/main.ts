import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import GlobalComponent from "./GlobalComponent.vue";

const app = createApp(App);

app.component('GlobalComponent', GlobalComponent);

app.mount('#app')
