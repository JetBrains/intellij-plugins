<template>
  <div id="app">
    <comp-u-i see-me="12345" butNotThis="112"></comp-u-i>
    <comp-u-i seeMe="12345" butNotThis="112"></comp-u-i>
  </div>
</template>
<script>
import CompUI from 'compUI.vue'
export default {
  components: {CompUI}
}
</script>