<template>
  <HW msg="foo"/>
  <<warning descr="Unknown html tag HW2">HW2</warning> msg="foo"/>
</template>
<script>
import HW from './a-component.vue'
import Vue from 'vue'

export default Vue.extend({
  name: 'app',
  components: {
    HW
  },
});
</script>