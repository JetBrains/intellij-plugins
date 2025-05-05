<script setup lang="ts">
defineEmits<{
  add: [id: number]
  change: { id: number, options: unknown }
  delete: { id: number }

  add_o: [id?: number]
  change_o: { id?: number, options?: unknown }
  delete_o: { id?: number }
}>()
</script>

<template>
  <div>
    <button @click="$emit<error descr="Argument types do not match parameters">('remove', 12)</error>">Add</button>

    <button @click="$emit('delete')">Delete</button>
  </div>
</template>
