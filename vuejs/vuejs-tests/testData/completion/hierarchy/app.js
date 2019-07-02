import Vue from 'vue'
import App from './App.vue'
import HelloWorld from './components/HelloWorld'

Vue.config.productionTip = false

Vue.component("HeyWorld", HelloWorld)

new Vue({
          el: '#app',
          components: {
            HelloApp: HelloWorld,
            App
          }
        })
