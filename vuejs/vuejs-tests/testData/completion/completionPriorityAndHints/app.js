import Vue from 'vue'
import App from './completionPriorityAndHints.vue'
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
