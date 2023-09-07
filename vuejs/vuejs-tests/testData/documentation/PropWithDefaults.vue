<script setup lang="ts">

export interface Props {
  items: string[]
}


const props = withDefaults(defineProps<Props>(), {
  items: () => [],
})

props.ite<caret>ms;
</script>

<template>
</template>