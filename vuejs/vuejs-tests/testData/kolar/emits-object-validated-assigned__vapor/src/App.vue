<script vapor lang="ts">
const emit = defineEmits({
  submit: (payload: { email: string }) => payload.email != null,
  click: null,
})

function onSubmit() {
  emit('submit', { email: 'a@b.com' })
}
</script>

<template>
  <button @click="emit('click')">Click</button>
  <button @click="onSubmit">Submit</button>
  <button @click="emit('submit', { email: 'a@b.com' })">Submit (direct)</button>
</template>
