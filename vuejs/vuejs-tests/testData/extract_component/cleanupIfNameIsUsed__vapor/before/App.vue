<script vapor>
defineProps({
  propWithCamel: {},
})
</script>

<template>
  <caret><p>Very first paragraph {{ propWithCamel }}</p>
  <p>Second paragraph {{ propWithCamel }}</p>
</template>
