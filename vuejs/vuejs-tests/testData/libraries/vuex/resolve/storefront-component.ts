import {mapActions, mapGetters, mapMutations, mapState} from 'vuex'
import {rootStore} from "aaa"
import {CART_ADD_ITEM, CART_DEL_ITEM} from "./store/cart/mutation-types"

export default {
  computed: {
    appliedCoupon(): false {
      return this.$store.getters['cart/getCoupon']
    },
    appliedCoupon2(): false {
      return rootStore.getters['cart/getCoupon']
    },
    getCurrentStoreView(): false {
      return this.$store.getters.getCurrentStoreView
    },
    aMap(): void {
      return this.$store.state.category.categoriesMap
    },
    ...mapGetters('category', {
      getCategories: 'getCategories',
      routes: 'cart/breadcrumbs/getBreadcrumbsRoutes',
      foo: 'foo'
    }),
    ...mapGetters({
      isVirtualCart: 'cart/isVirtualCart',
      foo: 'cart/foo',
    }),
    ...mapGetters('cart', ['getCartItems', 'foobar', 'breadcrumbs/getBreadcrumbsRoutes']),
    ...mapGetters(['cart/isCartConnected', 'category/bar']),

    ...mapState({
      foo2: 'foo',
      micro: 'cart/isMicrocartOpen',
      ship: 'shipping'
    }),
    ...mapState("cart", {
      foo3: 'foo',
      micro2: 'isMicrocartOpen',
      ship2: 'shipping'
    }),

    ...mapState('cart', ['isMicrocartOpen', 'shipping']),
    ...mapState(['cart/isMicrocartOpen', 'shipping']),

    ...mapState({
      routes: state => state.cart.breadcrumbs.routes,
      routes2(state) {
        return state.cart.breadcrumbs.routes
      },
      foo: state => state.cart.breadcrumbs.foo,
      ship: state => state.shipping
    }),
    ...mapState('cart', {
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
    applyCoupon(code: string): Promise<boolean> {
      return this.$store.dispatch('cart/applyCoupon', code)
    },
    applyCoupon2(code: string): Promise<boolean> {
      return rootStore.dispatch('cart/applyCoupon', code)
    },
    applyCoupon3(code: string): Promise<boolean> {
      return this.$store.dispatch({type: 'cart/applyCoupon', code})
    },
    applyCoupon4(code: string): Promise<boolean> {
      return rootStore.dispatch({type: 'cart/applyCoupon', code})
    },
    deleteItem(code: string): Promise<boolean> {
      return rootStore.commit(CART_DEL_ITEM, code)
    },
    deleteItem2(code: string): Promise<boolean> {
      return this.$store.commit(CART_DEL_ITEM, code)
    },
    setItem(code: string): Promise<boolean> {
      return rootStore.commit('cart/breadcrumbs/set', code)
    },
    setItem2(code: string): Promise<boolean> {
      return this.$store.commit('cart/breadcrumbs/set', code)
    },
    setItem3(code: string): Promise<boolean> {
      return rootStore.commit({type: 'cart/breadcrumbs/set', code})
    },
    setItem4(code: string): Promise<boolean> {
      return this.$store.commit({type: 'cart/breadcrumbs/set', code})
    },

    ...mapActions('cart/breadcrumbs', ['set']),
    ...mapActions(['cart/configureItem']),
    ...mapActions({
      fire: (dispatch) => dispatch('cart/configureItem'),
      fire2: (dispatch) => dispatch({type: 'cart/configureItem'})
    }),
    ...mapActions('cart', {
      fire: (dispatch) => dispatch('configureItem'),
      fire2: (dispatch) => dispatch({type: 'configureItem'})
    }),

    ...mapMutations([
      CART_ADD_ITEM,
      'cart/breadcrumbs/set'
    ]),
    ...mapMutations('cart', [
      'breadcrumbs/set']),
    ...mapMutations({
      set: commit => {
        commit('cart/breadcrumbs/set') //1
        commit({type: 'cart/breadcrumbs/set'}) //1
        {
          let commit = (s: string) => false
          commit('cart/breadcrumbs/set') //2
        }
      }
    }),
    ...mapMutations('cart/breadcrumbs', {
      set: commit => commit('set'),
      set2: commit => commit({type: 'set'})
    }),
  }
}
