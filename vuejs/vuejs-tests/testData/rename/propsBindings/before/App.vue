<template>
  <div id="app">
    <HelloWorld myMessage="Welcome to Your Vue.js App" :userId="userId"/>
    <HelloWorld my-message="Welcome to Your Vue.js App" :user-id="userId"/>
  </div>
</template>

<script>
import HelloWorld from './PropsBindings.vue'

export default {
  name: 'App',
  components: {
    HelloWorld
  },
  data: function () {
    return {
      userId: {id: 200}
    }
  }
}
</script>
