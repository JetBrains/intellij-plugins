<template>
  <caret><div>
    <other-comp>123 -> {{ prop }}</other-comp>
    <unknown-comp/>
  </div>
</template>
<script lang="ts">
import OtherComp from './OtherComp'

export default {
  name: 'current-comp',
  components: {OtherComp},
  props: ['prop']
}
</script>