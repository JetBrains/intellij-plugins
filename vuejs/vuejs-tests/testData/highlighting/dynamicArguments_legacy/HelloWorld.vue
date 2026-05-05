<script setup lang="ts">
import { ref } from 'vue'

defineProps<{ msg: string }>()

const count = ref(0)
</script>

<template>
  <with-slots @[bar] :[foo]>
    <template #[fooBar]="{}">
      Test
    </template>
    <template v-slot:[bar]>

    </template>
    <template #<weak_warning descr="Unrecognized slot name">footers</weak_warning>>
      Test
    </template>
  </with-slots>
</template>
