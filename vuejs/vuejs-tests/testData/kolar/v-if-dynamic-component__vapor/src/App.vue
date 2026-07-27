<script vapor lang="ts">
import MyCounter from './MyCounter.vue'
import MyLabel from './MyLabel.vue'

const showCounter = true
const Dynamic = showCounter ? MyCounter : MyLabel
</script>

<template>
  <component v-if="Dynamic" :is="Dynamic" />
</template>
