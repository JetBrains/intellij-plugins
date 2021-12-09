import Vue from 'vue'
import Vuex from 'vuex'
import marvel from './modules/marvel-store'
import dc from './modules/dc-store'

Vue.use(Vuex)

const modules = {
  marvel,
  dc,
}
export default new Vuex.Store({modules})
