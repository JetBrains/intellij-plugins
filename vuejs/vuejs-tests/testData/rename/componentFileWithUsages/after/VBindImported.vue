<script setup lang="ts">
import OrdersListView from "./OrdersListView.vue";

defineProps<{
  someCustomProp?: string
  inputProp?: string
}>()

const places: any[] = []
</script>

<template>
  <div class="c">{{ someCustomProp }}</div>

  <li v-for="place in places">
    <orders-list-view :place/>
    <OrdersListView :place/>
  </li>
</template>