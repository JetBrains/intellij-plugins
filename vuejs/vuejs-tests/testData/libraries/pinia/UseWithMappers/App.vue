<template>
  <div class='flex justify-center'>
    {{this.cartStore}}
    {{this.foo(12)}}
    {{this.formattedCart}}
    {{this.formattedCart[0].<error descr="Unresolved variable cst">cst</error>}}
    {{this.<error descr="Unresolved variable total">total</error>}}
    {{this.cartStore.formattedCart}}
    {{this.cartStore.<error descr="Unresolved variable formattedGart">formattedGart</error>}}
    {{cartStore}}
    {{foo(12)}}
    {{formattedCart}}
    {{formattedCart[0].<error descr="Unresolved variable cst">cst</error>}}
    {{<error descr="Unresolved variable or type total">total</error>}}
    {{cartStore.formattedCart}}
    {{cartStore.<error descr="Unresolved variable formattedGart">formattedGart</error>}}
  </div>
</template>

<script lang='ts'>
import {mapActions, mapState, mapStores} from 'pinia'
import {computed, defineComponent, onMounted, ref} from 'vue'
import {useCartStore} from './cart'

const diversion = {
  formattedGart: 12,
  cst: 12
}

export default defineComponent({
  methods: {
    ...mapActions(useCartStore, {foo: 'add'}),
    test() {
      this.foo(12)
    }
  },
  computed: {
    ...mapStores(useCartStore),
    ...mapState(useCartStore, ['formattedCart']),
  },
  created() {
    let a: string = <error descr="Initializer type  CartPreview[]  is not assignable to variable type  string ">this.cartStore.formattedCart</error>
    let b = this.cartStore.<error descr="Unresolved variable formattedGart">formattedGart</error>
    let c: string = <error descr="Initializer type  CartPreview[]  is not assignable to variable type  string ">this.formattedCart</error>
    this.formattedCart[0].<error descr="Unresolved variable cst">cst</error>
    let d: string = <error descr="Initializer type  number  is not assignable to variable type  string ">this.formattedCart[0].cost</error>
    this.<error descr="Unresolved variable total">total</error>
    this.<error descr="Unresolved function or method remove()">remove</error>(12)
    let <warning descr="Unused local variable s">s</warning> = a + b + c + d
  }
})

</script>
