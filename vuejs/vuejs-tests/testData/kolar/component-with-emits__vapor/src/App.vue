<script vapor lang="ts">
import MyCounter from './MyCounter.vue'
import MyLabel from './MyLabel.vue'

let count = 1
</script>

<template>
  {{ count }}
  
  <MyCounter @change="count = $event.value"/>
  <MyLabel @select="count = 0"/>
</template>