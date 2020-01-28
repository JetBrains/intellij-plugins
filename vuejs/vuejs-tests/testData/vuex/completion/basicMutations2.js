export const store = new Vuex.Store(
    {
      mutations: {
        mutation1(state, payload) {
          let data = {
            mutation1_inside: "uno",
            mutation2_inside: "duos"
          }
        }
        , mutation2(state, payload) {
        }
      },
      actions: {
        action1: function ({commit}, payload) {
          commit('m<caret>')
        }
      }
    })
