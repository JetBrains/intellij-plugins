import {createNamespacedHelpers} from 'vuex'

const {mapState, mapActions, mapGetters, mapMutations} = createNamespacedHelpers('cart')

const categoryModule = createNamespacedHelpers('category')

export default {
  computed: {
    ...mapGetters({
      routes: 'breadcrumbs/getBreadcrumbsRoutes',
      isVirtualCart: 'isVirtualCart',
      foo: 'cart/foo',
    }),
    ...categoryModule.mapGetters([
      'getLastTotalsSyncDate',
      'cart/getLastTotalsSyncDate',
      'getCategoryProducts', 'foo'
    ]),
    ...mapGetters(['getCartItems', 'foobar', 'breadcrumbs/getBreadcrumbsRoutes']),

    ...mapState({
      foo3: 'foo',
      micro2: 'isMicrocartOpen',
      ship2: 'shipping'
    }),

    ...mapState(['isMicrocartOpen', 'shipping']),

    ...mapState({
      routes2: state => state.breadcrumbs.routes,
      foo2: state => state.breadcrumbs.foo,
      ship2: state => state.shipping,
      micro: state => state.isMicrocartOpen,
      shippingMethod: (state, getters) => getters.getShippingMethod,
      shippingMethod2(state, getters) {
        return getters.getShippingMethod
      }
    })
  },
  methods: {
    ...mapActions(['breadcrumbs/set', 'configureItem']),
    ...mapActions({
      fire: (dispatch) => dispatch('configureItem'),
      fire2: (dispatch) => dispatch({type: 'configureItem'})
    }),

    ...mapMutations(['breadcrumbs/set']),

    ...mapMutations({
      set: commit => {
        commit('breadcrumbs/set') //1
        commit({type: 'breadcrumbs/set'})
        {
          let commit = (s: string) => false
          commit('breadcrumbs/set') //2
        }
      }
    })
  }
}
