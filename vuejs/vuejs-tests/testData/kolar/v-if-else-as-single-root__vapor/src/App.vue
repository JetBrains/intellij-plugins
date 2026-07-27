<script vapor lang="ts">
import { ref } from 'vue'
import Base from './Base.vue'

const baseRef = ref<InstanceType<typeof Base>>()
</script>

<template>
  <Base ref="baseRef" class="extra" />
</template>
