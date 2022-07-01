import Vue from 'vue'
import App from './App.vue'
import App2 from "./App2"

Vue.config.productionTip = false

new Vue({
  el: '#app',
  components: {
    App,
    App2
  },
  filters: {
    appFilter: function (value, param) { return "" }
  }
})

Vue.filter("globalFilter", function (value) { return 12 })

const filterDefinition = function (value) { return 42 };

const danger = {
  filterDefinition: function (value) { return "oops" }
}

Vue.filter("globalReferencedFilter", filterDefinition)

Vue.filter("globalQualifiedReferencedFilter", danger.filterDefinition) // unsupported
