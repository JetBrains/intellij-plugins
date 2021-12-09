import Vue from 'vue'
import Vuex from 'vuex'
import counter from "./counter"

Vue.use(Vuex)

export default new Vuex.Store({
    state: {},
    mutations: {},
    actions: {},
    modules: {
        counter
    }
})
