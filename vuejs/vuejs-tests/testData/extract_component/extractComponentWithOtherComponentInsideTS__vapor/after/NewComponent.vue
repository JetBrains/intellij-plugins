<script vapor lang="ts">
import OtherComp from './OtherComp'

defineProps({
  prop: {},
})
</script>

<template>
  <div>
    <other-comp>123 -> {{ prop }}</other-comp>
    <unknown-comp/>
  </div>
</template>