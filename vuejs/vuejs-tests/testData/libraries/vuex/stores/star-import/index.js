import * as replenish from './modules/replenish.js'
import * as sour from './modules/sour'


export default new Vuex.Store(
    {
      modules: {replenish, sour},
      state: {
        nonceOutdated: false,
        showNewVersion: false,
        newAppVersion: null,
      },
      actions: {
        setAuthHeaders({state, getters, dispatch}) {

        },
        setAxiosInterceptors({state, commit}) {

        },
      },
      getters: {
        userLoggedIn(state) {
          return null !== state.user
        },
        userFirstName(state, getters) {
          return getters.userLoggedIn ? state.user.firstName : '';
        }
      },
    })
