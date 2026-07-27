<script setup lang="ts">
interface User {
  profile?: {
    active?: boolean
    name: string
  }
}

const user: User = { profile: { active: true, name: 'Ada' } }
</script>

<template>
  <span v-if="user.profile?.active">{{ user.profile.name }}</span>
</template>
