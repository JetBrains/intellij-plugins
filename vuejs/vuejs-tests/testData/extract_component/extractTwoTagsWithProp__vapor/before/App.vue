<script vapor>
defineProps({
  one: {},
})
</script>

<template>
  <selection><p>Paragraph! {{ one + 1 }}</p>
    <div>And div {{ unresolved }}</div></selection>
</template>
