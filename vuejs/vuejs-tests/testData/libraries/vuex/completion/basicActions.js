export const store = new Vuex.Store(
    {
      actions: {
        action1: function ({commit}, payload) {
          commit('mutation1')
          let data = {
            insideAction1: "uno",
            insideAction2: "duos"
          }
        },
        action_2: function ({commit}) {
          commit('mutation_2')
        },
      },
    })
