<script vapor lang="ts">
defineEmits<{
  select: [],
}>()
</script>

<template>
  <span>My label!</span>
  <button @click="emit('select')">Select</button>
</template>
