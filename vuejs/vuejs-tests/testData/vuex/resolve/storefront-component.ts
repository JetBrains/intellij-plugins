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
    aMap(): void {
      return this.$store.state.category.categoriesMap
    },
    filtersMap(): void {
      return rootStore.state.category.filtersMap
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
      ship: state => state.shipping,
      micro: state => state.isMicrocartOpen,
      shippingMethod: (state, getter) => getter.getShippingMethod,
      shippingMethod2(state, getter) {
        return getter.getShippingMethod
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

    ...mapActions('cart/breadcrumbs', ['set']),
    ...mapActions(['cart/configureItem']),
    ...mapActions({
      fire: (dispatch) => dispatch('cart/configureItem')
    }),
    ...mapActions('cart', {
      fire: (dispatch) => dispatch('configureItem')
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
        {
          let commit = (s: string) => false
          commit('cart/breadcrumbs/set') //2
        }
      }
    }),
    ...mapMutations('cart/breadcrumbs', {
      set: commit => commit('set')
    }),
  }
}
