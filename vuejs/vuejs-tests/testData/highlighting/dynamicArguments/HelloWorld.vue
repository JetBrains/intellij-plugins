<script setup lang="ts">
import { ref } from 'vue'

defineProps<{ msg: string }>()

const count = ref(0)
</script>

<template>
  <with-slots <error descr="Vue: v-on is missing expression.">@[bar]</error> <error descr="Vue: v-bind is missing expression.">:<error descr="Vue: v-bind with same-name shorthand only allows static argument.">[foo]</error></error>>
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
