<script setup>
defineProps({
  one: {},
})

function compMethod() {
  // compute
}
</script>

<template>
  <caret><p v-if="one">Paragraph! {{ compMethod() }}</p>
</template>
