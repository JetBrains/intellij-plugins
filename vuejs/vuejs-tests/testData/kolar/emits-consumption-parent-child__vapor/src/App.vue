<script vapor lang="ts">
import MyCounter from './MyCounter.vue'

let count = 1
</script>

<template>
  {{ count }}

  <MyCounter @change="count = $event" @reset="count = 0"/>
</template>
