<script setup lang="ts">
import MyCounter from './MyCounter.vue'
import MyLabel from './MyLabel.vue'

const count = 1
</script>

<script>
export interface MyUser {
  name: string
  age: number
}

export type OtherUser = Readonly<{
  name: string
  age: number
}>
</script>

<template>
  {{ count }}
  
  <MyCounter/>
  <MyLabel/>
</template>