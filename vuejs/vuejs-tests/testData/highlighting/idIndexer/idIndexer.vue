<template>
  <div id="app">
    <button @click="go$">go$</button>
    <button @click="click">click</button>
  </div>
</template>

<script>
export default {
  name: 'App',
  methods: {
    go$() {
      console.log("go$")
    },
    <warning descr="Unused function go_">go_</warning>() {

    },
    click() {
      this.go$()
    },
  },
}
</script>
