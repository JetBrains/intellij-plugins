import Vue from 'vue'
import Component from 'vue-class-component'
import {Action, Getter, Mutation, namespace, State} from 'vuex-class'

const cartModule = namespace('cart')

@Component
export class MyComp extends Vue {
  @Getter('cart/isVirtualCart') vCart1
  @Getter('isVirtualCart') vCart2
  @State('cart/isMicrocartOpen') microcart
  @State('shipping') shipping1
  @State('foobar') foobar2
  @State('cart/breadcrumbs') foo
  @State(state => state.cart.breadcrumbs.routes) bcRoutes2
  @Action('cart/configureItem') configItem
  @Mutation('cart/breadcrumbs/set') bcSet

  @cartModule.Getter('getCartItems') cartItems
  @cartModule.Getter('foobar') foobar
  @cartModule.Getter('breadcrumbs/getBreadcrumbsRoutes') breadcrumbRoutes
  @cartModule.State('isMicrocartOpen') micro2
  @cartModule.State('shipping') shipping2
  @cartModule.State(state => state.breadcrumbs.routes) bcRoutes3
  @cartModule.Action('breadcrumbs/set') bcSetAction
  @cartModule.Mutation('breadcrumbs/set') bcSet2


}
