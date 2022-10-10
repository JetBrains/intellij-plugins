<template>
  <n-button :color='colorValue'
            :block="<weak_warning descr="Type string is not assignable to type boolean">colorValue</weak_warning>">
  </n-button>
</template>

<script setup lang="ts">
import { NButton } from 'naive-ui';
import { ref } from "vue";

const colorValue = ref<string>('black');

</script>
