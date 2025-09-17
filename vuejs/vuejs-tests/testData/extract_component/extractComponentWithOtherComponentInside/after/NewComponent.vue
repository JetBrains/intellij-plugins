<template>
  <div>
    <other-comp>123 -> {{ prop }}</other-comp>
  </div>
</template>
<script>
import OtherComp from './OtherComp'

export default {
  name: 'NewComponent',
  components: {OtherComp},
  props: {
    prop: {}
  }
}
</script>