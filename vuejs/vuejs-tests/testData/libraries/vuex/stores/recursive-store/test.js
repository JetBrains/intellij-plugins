import Vuex from "vuex"

let config = clone_deep(defaultStore)

new Vuex.Store({
  ...config,
  actions: {
    ...config.actions,
    someProp: someValue
  }
})
