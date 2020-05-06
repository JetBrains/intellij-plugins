import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

new Vuex.Store(
    {
      state: {
        state1: 12
      },
      getters: {
        bar: function(state) {
          return state.state1
        }
      },
      mutations: {
        foo: function() {

        }
      },
      actions: {
        action1: function ({commit, getters}, payload) {
          commit('foo', getters.bar)
        }
      }
    })
