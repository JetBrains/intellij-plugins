<script vapor lang="ts">
const props = defineProps(['count'])
</script>

<template>
  {{ props.count }}
  <div :title="props.count">
    <span>{{ props.count }}</span>
  </div>
</template>
