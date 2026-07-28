<script vapor lang="ts">
const title = defineModel<string>('title', { default: 'Untitled' })
const count = defineModel<number>('count', { required: true })
</script>

<template>
  {{ title }} - {{ count }}
</template>
