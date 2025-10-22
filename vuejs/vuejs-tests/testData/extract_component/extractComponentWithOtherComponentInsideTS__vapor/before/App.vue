<script vapor lang="ts">
import OtherComp from './OtherComp'

defineProps(['prop'])
</script>

<template>
  <caret><div>
    <other-comp>123 -> {{ prop }}</other-comp>
    <unknown-comp/>
  </div>
</template>
