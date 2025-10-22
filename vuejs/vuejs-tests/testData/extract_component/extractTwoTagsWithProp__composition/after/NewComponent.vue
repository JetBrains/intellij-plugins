<script setup>
defineProps({
  one: {},
})
</script>

<template>
  <p>Paragraph! {{ one + 1 }}</p>
  <div>And div {{ unresolved }}</div>
</template>