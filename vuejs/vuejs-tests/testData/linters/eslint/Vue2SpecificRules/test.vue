<template>
  <div id="app">
    <img alt="Vue logo" src="./assets/logo.png">
    <HelloWorld  v-bind:msg='$props' <error descr="ESLint: Duplicate attribute 'msg'. (vue/no-duplicate-attributes)">msg= "Welcome to Your Vue.js App"</error>/>
  </div>
  <error descr="ESLint: The template root requires exactly one element. (vue/no-multiple-template-root)"><img alt="Vue logo" src="./assets/logo.png"></error>
</template>

<script>
import HelloWorld from './components/HelloWorld.vue'

const <error descr="ESLint: 'a' is assigned a value but never used. (no-unused-vars)">a</error> = 12

export default {
  name: 'App',
  components: {
    HelloWorld
  }
}
</script>
