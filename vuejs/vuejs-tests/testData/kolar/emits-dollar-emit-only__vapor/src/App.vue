<script vapor lang="ts">
defineEmits<{
  submit: [payload: { email: string }]
}>()
</script>

<template>
  <button @click="$emit('submit', { email: 'a@b.com' })">Submit</button>
</template>
