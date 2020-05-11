import Vue from "vue"
import {mapActions, mapGetters, mapMutations, mapState, createNamespacedHelpers} from 'vuex'

const categoryModule = createNamespacedHelpers('category')

export default Vue.extend({
    computed: {
        ...mapGetters('category', {
            getCategories: 'getCategories',
            routes: 'cart/breadcrumbs/getBreadcrumbsRoutes',
            foo: 'foo'
        }),
        ...categoryModule.mapGetters([
                                         'getLastTotalsSyncDate',
                                         'cart/getLastTotalsSyncDate',
                                         'getCategoryProducts', 'getfoo2'
                                     ]),
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
            routes4: state => state.breadcrumbs.routes,
            foo4: state => state.breadcrumbs.foo,
            ship4: state => state.shipping,
            micro4: state => state.isMicrocartOpen,
            shippingMethod: (state, getters) => getters.getShippingMethod,
            shippingMethod2(state, getters) {
                return getters.getShippingMethod
            }
        })
    },
    methods: {
        ...mapActions('cart', ['configureItem']),
        ...mapActions(['cart/breadcrumbs/set', 'cart/configureItem']),
        ...mapActions('cart', {
            configItem: (dispatch, arg) => dispatch('configureItem')
        }),
        ...mapActions('cart', {
            configItemMap: 'configureItem'
        }),
        ...mapActions({
                          configItemN: (dispatch, arg) => dispatch('cart/configureItem')
                      }),
        ...mapMutations('cart', ['breadcrumbs/set']),
        ...mapMutations('cart', {
            cartMutFun: () => 12
        }),
        ...categoryModule.mapActions(['loadCategoryProducts']),
        ...categoryModule.mapActions({
                                         catAction: (dispatch, arg) => dispatch('loadCategoryProducts')
                                     }),

        check: function () {
            this.<caret>
        }
    }
})