<script setup>
import NewComponent from "./NewComponent.vue";

defineProps(['prop'])
</script>

<template>
  <NewComponent :prop="prop"/>
</template>
