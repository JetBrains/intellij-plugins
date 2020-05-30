<script lang="ts">
import {mapActions, mapGetters, mapMutations, mapState} from 'vuex'
import {rootStore} from "aaa"

export default {
    computed: {
        appliedCoupon(): false {
            this.$store.getters['cart/getCoupons']
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
            routes: '<weak_warning descr="Unknown Vuex module namespace category/cart">cart</weak_warning>/breadcrumbs/getBreadcrumbsRoutes',
            foo: '<weak_warning descr="Cannot resolve Vuex getter foo">foo</weak_warning>'
        }),
        ...mapGetters({
            isVirtualCart: 'cart/isVirtualCart',
            foo: 'cart/<weak_warning descr="Cannot resolve Vuex getter foo">foo</weak_warning>',
        }),
        ...mapGetters('<weak_warning descr="Unknown Vuex module namespace carts">carts</weak_warning>', ['<weak_warning descr="Cannot resolve Vuex getter getCartItems">getCartItems</weak_warning>', '<weak_warning descr="Cannot resolve Vuex getter foobar">foobar</weak_warning>', '<weak_warning descr="Unknown Vuex module namespace carts/breadcrumbs">breadcrumbs</weak_warning>/getBreadcrumbsRoutes']),
        ...mapGetters(['cart/isCartConnected', 'category/<weak_warning descr="Cannot resolve Vuex getter bar">bar</weak_warning>']),

        ...mapState({
            foo2: '<weak_warning descr="Cannot resolve Vuex state foo">foo</weak_warning>',
            micro: 'cart/isMicrocartOpen',
            micro1: 'cart/<weak_warning descr="Cannot resolve Vuex state isMicrocartOpen2">isMicrocartOpen2</weak_warning>',
            ship: 'shipping'
        }),
        ...mapState("cart", {
            foo3: '<weak_warning descr="Cannot resolve Vuex state foo">foo</weak_warning>',
            micro2: 'isMicrocartOpen',
        }),
    },
    methods: {
        applyCoupon(code: string): Promise<boolean> {
            return this.$store.dispatch('cart/applyCoupon', code)
        },
        applyCoupon2(code: string): Promise<boolean> {
            return rootStore.dispatch('cart/applyCoupons', code)
        },
        applyCoupon3(code: string): Promise<boolean> {
            return this.$store.dispatch({type: 'cart/applyCoupon', code})
        },
        applyCoupon4(code: string): Promise<boolean> {
            return rootStore.dispatch({type: 'carts/applyCoupon', code})
        },
        setItem(code: string): Promise<boolean> {
            return rootStore.commit('cart/breadcrumbs/sets', code)
        },
        setItem2(code: string): Promise<boolean> {
            return this.$store.commit('carts/breadcrumbs/set', code)
        },

        ...mapActions('cart/breadcrumbs', ['set']),
        ...mapActions(['cart/<weak_warning descr="Cannot resolve Vuex action configureItems">configureItems</weak_warning>']),
        ...mapActions({
            fire: (dispatch) => dispatch('cart/configureItem'),
            fire2: (dispatch) => dispatch({type: 'carts/configureItem'})
        }),
        ...mapActions('<weak_warning descr="Unknown Vuex module namespace carts">carts</weak_warning>', {
            fire: (dispatch) => dispatch('configureItem'),
            fire2: (dispatch) => dispatch({type: 'configureItem'})
        }),

        ...mapMutations([
            'cart/breadcrumbs/<weak_warning descr="Cannot resolve Vuex mutation sets">sets</weak_warning>',
            'cart/breadcrumbs/set'
        ]),
        ...mapMutations('<weak_warning descr="Unknown Vuex module namespace carts">carts</weak_warning>', [
            '<weak_warning descr="Unknown Vuex module namespace carts/breadcrumbs">breadcrumbs</weak_warning>/set']),
    }
}
</script>