<script setup lang="ts">
import MyCounter from './MyCounter.vue'
import MyLabel from './MyLabel.vue'

const count = 1
</script>

<template>
  {{ count }}
  
  <MyCounter/>
  <MyLabel/>
</template>