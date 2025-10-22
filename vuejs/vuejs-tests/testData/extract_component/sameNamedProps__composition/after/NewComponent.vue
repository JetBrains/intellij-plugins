<script setup>
defineProps({
  propWithCamel: {},
})
</script>

<template>
  <p>Very first paragraph {{ propWithCamel }}</p>
  <p>Second paragraph {{ propWithCamel }}</p>
</template>