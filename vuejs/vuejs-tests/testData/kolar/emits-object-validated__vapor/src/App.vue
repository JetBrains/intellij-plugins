<script vapor lang="ts">
defineEmits({
  submit: (payload: { email: string }) => payload.email != null,
  click: null,
})
</script>

<template>
  <button @click="$emit('click')">Click</button>
  <button @click="$emit('submit', { email: 'a@b.com' })">Submit</button>
</template>
