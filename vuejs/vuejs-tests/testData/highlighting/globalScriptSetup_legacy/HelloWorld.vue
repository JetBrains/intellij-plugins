<script setup lang="ts">
import { ref } from 'vue'

defineProps<{ msg: string }>()

const count = ref(0)
</script>

<template>
  <with-slots>
    <template #footer>
      Test
    </template>
    <template #<weak_warning descr="Unrecognized slot name">footers</weak_warning>>
      Test
    </template>
  </with-slots>
</template>
