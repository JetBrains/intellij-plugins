<script lang="ts">
import OtherComp from './OtherComp'

export default {
  name: 'NewComponent',
  components: {OtherComp},
  props: {
    prop: {}
  }
}
</script>

<template>
  <div>
    <other-comp>123 -> {{ prop }}</other-comp>
    <unknown-comp/>
  </div>
</template>