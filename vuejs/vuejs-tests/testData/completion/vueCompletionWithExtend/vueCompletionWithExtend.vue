<template>
  <HW <caret>/>
</template>
<script>
import Vue from "vue"
import HW from './a-component.vue'

export default Vue.extend({
  name: 'app',
  components: {
    HW
  },
});
</script>