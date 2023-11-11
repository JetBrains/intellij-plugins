<script lang="ts">
import {defineComponent, SlotsType} from 'vue'

export default defineComponent(
  {
    slots: Object as SlotsType<{
      default: { foo: string; bar: number }
      header: { company: string }
      item: { data: {year?: number} }
    }>
  }
)
</script>

<template>
  <slot name="default"></slot>
  <slot name="item"></slot>
  <slot name="header"></slot>
</template>
