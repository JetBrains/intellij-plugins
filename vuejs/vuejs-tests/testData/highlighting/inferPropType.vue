<template>
  <n-button :color='colorValue'
            :block="<error descr="Type  string  is not assignable to type  boolean ">colorValue</error>">
  </n-button>
</template>

<script setup lang="ts">
import { NButton } from 'naive-ui';
import { ref } from "vue";

const colorValue = ref<string>('black');

</script>
