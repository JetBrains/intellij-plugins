<script setup>
import {reactive} from 'vue'
import NewComponent from "./NewComponent.vue";

const items = reactive([1, 2, 3, 4, 5])
</script>

<template>
  <div v-for="item in items">
    <NewComponent :item="item"/>
  </div>
</template>
