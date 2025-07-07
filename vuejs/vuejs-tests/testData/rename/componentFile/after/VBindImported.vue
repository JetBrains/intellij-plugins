<script setup lang="ts">
import SomeComponent from "./OrdersListView.vue";

defineProps<{
  someCustomProp?: string
  inputProp?: string
}>()

const places: any[] = []
</script>

<template>
  <div class="c">{{ someCustomProp }}</div>

  <li v-for="place in places">
    <some-component :place/>
    <SomeComponent :place/>
  </li>
</template>