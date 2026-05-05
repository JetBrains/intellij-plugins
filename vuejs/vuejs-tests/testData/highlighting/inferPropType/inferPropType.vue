<template>
  <n-button :color='colorValue'
            :block="colorValue">
  </n-button>
</template>

<script setup lang="ts">
import { NButton } from 'naive-ui';
import { ref } from "vue";

const colorValue = ref<string>('black');

</script>
