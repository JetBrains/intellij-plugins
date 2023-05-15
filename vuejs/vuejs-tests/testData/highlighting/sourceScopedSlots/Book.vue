<template>
  <slot name="scopedSlot" :foo="bar"></slot>
</template>

<script setup lang="ts">

interface balProps {
  bar: string | number
}

const balloonProps = withDefaults(defineProps<balProps>(), {
  bar: 'foo',
});
</script>
