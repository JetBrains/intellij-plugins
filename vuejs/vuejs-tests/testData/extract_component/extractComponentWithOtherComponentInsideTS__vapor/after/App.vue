<script vapor lang="ts">
import NewComponent from "./NewComponent.vue";

defineProps(['prop'])
</script>

<template>
  <NewComponent :prop="prop"/>
</template>
