<script setup lang='ts'>
import SomeComponent from './SomeComponent.vue'

const inputProp = 'abc'
</script>

<template>
  <SomeComponent v-bind:inputProp/>
</template>