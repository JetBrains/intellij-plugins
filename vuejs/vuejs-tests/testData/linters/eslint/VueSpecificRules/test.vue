<template>
  <div id="app">
    <img alt="Vue logo" src="./assets/logo.png">
    <HelloWorld  v-bind:msg='$props' <error descr="ESLint: Duplicate attribute 'msg'. (vue/no-duplicate-attributes)">msg</error>= "Welcome to Your Vue.js App"/>
  </div>
  <error descr="ESLint: The template root requires exactly one element. (vue/no-multiple-template-root)"><</error>img alt="Vue logo" src="./assets/logo.png">
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
