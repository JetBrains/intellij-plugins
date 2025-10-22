<script setup>
defineProps({
  compMethod: {},
  one: {},
})
</script>

<template>
  <p v-if="one">Paragraph! {{ compMethod }}</p>
</template>