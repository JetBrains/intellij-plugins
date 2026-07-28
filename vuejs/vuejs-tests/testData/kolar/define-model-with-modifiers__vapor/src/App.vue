<script vapor lang="ts">
const [text, modifiers] = defineModel<string>({
  set(value) {
    return modifiers.trim ? value.trim() : value
  }
})
</script>

<template>
  <input v-model="text" />
</template>
