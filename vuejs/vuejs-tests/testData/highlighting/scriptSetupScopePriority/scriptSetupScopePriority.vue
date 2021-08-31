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
  {{item.<weak_warning descr="Unresolved variable value">value</weak_warning>}}
</template>
