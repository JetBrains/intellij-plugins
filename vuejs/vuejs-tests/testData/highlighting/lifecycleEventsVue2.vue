<template>
  <div>
    <button v-on:click="decrement">-</button>
    {{ count }}
    <button v-on:click="increment">+</button>
  </div>
</template>

<script>
import Vue from 'vue'
import Component from 'vue-class-component'

// Define the component in class-style
@Component({
             mounted() {

             },
             <warning descr="Unused function bar">bar</warning>() {

             }
           })
export default class Counter extends Vue {
  // Class properties will be component data
  count = 0

  created() {
  }

  <warning descr="Unused method foo">foo</warning>() {

  }

  // Methods will be component methods
  increment() {
    this.count++
  }

  decrement() {
    this.count--
  }
}
</script>
