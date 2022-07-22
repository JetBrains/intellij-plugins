import { createStore } from 'vuex'
import counterModule from './counter/index'

const store = createStore({
  modules: {
    counterMod: counterModule
  }
})

export default store
