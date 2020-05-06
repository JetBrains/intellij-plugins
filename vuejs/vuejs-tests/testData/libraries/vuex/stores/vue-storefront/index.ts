import Vue from 'vue'
import Vuex from 'vuex'
import RootState from './RootState'
import {once} from '@vue-storefront/core/helpers'
import actions from './actions'
import getters from './getters'
import mutations from './mutations'
import {cartStore} from "./cart"
import {categoryModule} from "./category"

once('__VUE_EXTEND_VUEX__', () => {
  Vue.use(Vuex)
})

const state = {
  version: '',
  __DEMO_MODE__: false,
  config: {},
  cart: {},
  checkout: {},
  cms: {},
  compare: {},
  product: {},
  shipping: {},
  user: {},
  ui: {},
  newsletter: {},
  wishlist: {},
  attribute: {
    list_by_code: {},
    list_by_id: {},
    blacklist: [],
    labels: {}
  },
  category: {
    current_path: '',
    current_product_query: {},
    current: {
      slug: '',
      name: ''
    },
    filters: {}
  },
  stock: {
    cache: []
  },
  storeView: {},
  twoStageCachingDelta1: 0,
  twoStageCachingDelta2: 0,
  twoStageCachingDisabled: false,
  userTokenInvalidated: null,
  userTokenInvalidateAttemptsCount: 0,
  userTokenInvalidateLock: 0
}

let rootStore = new Vuex.Store<RootState> ({
  state,
  actions,
  getters,
  mutations,
  modules: {
    category: categoryModule,
  }
})

export default rootStore

rootStore.registerModule("cart", cartStore)
