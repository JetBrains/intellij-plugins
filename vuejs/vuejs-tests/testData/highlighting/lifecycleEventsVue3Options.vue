<template>
  <div>
    <button v-on:click="decrement">-</button>
    <button v-on:click="increment">+</button>
  </div>
</template>

<script>
export default {
  name: "App",
  setup() {

  },
  unmounted() {

  },
  <warning descr="Unused function bar">bar</warning>() {

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
};
</script>
