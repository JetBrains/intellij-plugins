import Vue from 'vue'
import App from './filters.vue'
import App2 from "./App2";

Vue.config.productionTip = false

new Vue({
          el: '#app',
          components: {
            App,
            App2
          },
          filters: {
            appFilter: function (value, param) {
              return ""
            }
          }
        })

Vue.filter("globalFilter", function (value) {
  return 12
})
