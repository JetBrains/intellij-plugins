<template>
  <div id="app">
    <img src="./assets/logo.png">
    <Hello<caret>World msg="Welcome to Your Vue.js App"/>
  </div>
</template>

<script>
export default {
  name: 'app',
  components: {
    HelloWorld: () => import('./index.vue')
  }
}
</script>