<script setup lang="ts">
import {PropType} from "vue";

defineModel('alignment', {
  type: String as PropType<'left' | 'right' | 'center'>,
})
</script>

<template>
  <div>{{ alignment }}</div>
</template>
