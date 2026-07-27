<script vapor lang="ts">
interface Form {
  name: string
}

const form: Form | null = { name: '' }
</script>

<template>
  <input v-if="form" v-model="form.name" />
</template>
