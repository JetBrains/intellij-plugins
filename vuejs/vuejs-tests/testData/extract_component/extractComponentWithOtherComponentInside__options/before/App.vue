<script>
import OtherComp from './OtherComp'

export default {
  name: 'current-comp',
  components: {OtherComp},
  props: ['prop']
}
</script>

<template>
  <caret><div>
    <other-comp>123 -> {{ prop }}</other-comp>
  </div>
</template>
