<template>
  <div>
    <object-literal-property :foo='{<caret>}' bar=""></object-literal-property>
  </div>
</template>

<script setup lang="ts">

const props = defineProps<{
  foo: {
    afo: number,
    boo: string,
  },
}>();

</script>