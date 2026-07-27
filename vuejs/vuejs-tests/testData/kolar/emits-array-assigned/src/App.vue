<script setup lang="ts">
const emit = defineEmits(['inFocus', 'submit'])

function onSubmit() {
  emit('submit')
}
</script>

<template>
  <button @focus="emit('inFocus')">Focus</button>
  <button @click="onSubmit">Submit</button>
  <button @click="emit('submit')">Submit (direct)</button>
</template>
