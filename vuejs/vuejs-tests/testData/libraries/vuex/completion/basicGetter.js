export const store = new Vuex.Store(
    {
      getters: {
        getter1(state) {
          let data = {
            insideGetter1: "uno",
            insideGetter2: "duos"
          }
        },
        getter_2(state) {
        }
      }

    })
