<script vapor lang="ts">
import { ref } from 'vue'

const isLoggedIn = ref(true)
const isAdmin = ref(false)
</script>

<template>
  <button @click="isLoggedIn = !isLoggedIn">Toggle logged in</button>
  <button @click="isAdmin = !isAdmin">Toggle admin</button>
  <div v-if="isLoggedIn && isAdmin">Admin panel</div>
  <div v-else-if="isLoggedIn || isAdmin">Limited panel</div>
</template>
