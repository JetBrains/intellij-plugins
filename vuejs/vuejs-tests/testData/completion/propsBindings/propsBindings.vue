<template>
  <div id="app">
   <HelloWorld my<caret> :userId="userId"/>
  </div>
</template>

<script>
import HelloWorld from './components/HelloWorld.vue'

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
