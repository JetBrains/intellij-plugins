<script vapor lang="ts">
defineEmits(['inFocus', 'submit'])
</script>

<template>
  <button @focus="$emit('inFocus')">Focus</button>
  <button @click="$emit('submit')">Submit</button>
</template>
