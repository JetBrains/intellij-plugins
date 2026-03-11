<script setup lang="ts">
interface User {
  name: string
  age: number
}
const user: User = { name: 'Alice', age: 30 }
const email = user.email
</script>
<template>
  <div>{{ user.name }}</div>
</template>
