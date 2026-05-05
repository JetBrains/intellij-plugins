<script lang="ts" setup>
import type {PropType} from "vue";
import type {Item} from "./Item";
const props = defineProps({
  item: {
    type: Object as PropType<Item>,
  }
})
</script>
<template>
  {{item.name}}
  {{item.value}}
</template>
