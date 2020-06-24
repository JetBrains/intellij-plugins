import {store} from "a"
import {mapActions} from "vuex"

store.registerModule("foo", {
  namespaced: true,
  actions: {
    namespacedAction(context) {

    },
    rootAction: {
      root: true,
      handler(context, payload) {
      }
    }
  },
  modules: {
    "inner": {
      namespaced: true,
      actions: {
        namespacedAction: {
          root: false,
          handler(context, payload) {

          }
        },
        innerRootAction: {
          root: true,
          handler(context, payload) {
          }
        }
      }
    }
  }
})

export default {
  computed: {
    ...mapActions(['rootAction',
      'innerRootAction',
      'namespacedAction',
      'foo/namespacedAction',
      'foo/rootAction',
      'foo/innerRootAction',
      'foo/inner/namespacedAction',
      'foo/inner/innerRootAction',
      'foo/inner/rootAction'
    ])
  }
}
