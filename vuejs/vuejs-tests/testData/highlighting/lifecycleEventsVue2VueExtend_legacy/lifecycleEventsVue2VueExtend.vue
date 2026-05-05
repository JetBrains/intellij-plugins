<template>
  <div>
    <button v-on:click="decrement">-</button>
    <button v-on:click="increment">+</button>
  </div>
</template>

<script>
import Vue from 'vue';
export default Vue.extend({
  name: "App",
  mounted() {

  },
  methods: {
    increment() {
      this.count++
    },
    decrement() {
      this.count--
    },
    <warning descr="Unused function foo">foo</warning>() {

    }
  }
});
</script>
