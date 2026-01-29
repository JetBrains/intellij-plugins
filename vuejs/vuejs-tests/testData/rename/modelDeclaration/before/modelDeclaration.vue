<script setup lang="ts">
import {PropType} from "vue";

defineModel('align', {
  type: String as PropType<'left' | 'right' | 'center'>,
})
</script>

<template>
  <div>{{ ali<caret>gn }}</div>
</template>
