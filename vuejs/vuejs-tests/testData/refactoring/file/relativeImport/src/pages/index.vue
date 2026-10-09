<script setup lang="ts">
import Hello from "../components/Hello.vue";
import { items } from "../components/animals.js";
</script>

<template>
  <Hello/>
  <ul>
    <li v-for="item in items" :key="item">{{ item }}</li>
  </ul>
</template>
