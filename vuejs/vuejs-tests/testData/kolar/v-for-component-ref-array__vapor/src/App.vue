<script vapor lang="ts">
import { ref } from 'vue'
import MyCounter from './MyCounter.vue'

const items = [1, 2, 3]
const counterRefs = ref<InstanceType<typeof MyCounter>[]>()
</script>

<template>
  <MyCounter v-for="item in items" :key="item" :count="item" ref="counterRefs" />
</template>
