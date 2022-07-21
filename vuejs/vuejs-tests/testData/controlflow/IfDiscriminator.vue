<script setup lang="ts">
type Named = { type: "Named"; name: string };
type Aged = { type: "Aged"; age: number };
type Union = Named | Aged;

const items: Union[] = [
  { type: "Named", name: "John" },
  { type: "Aged", age: 42 },
];
</script>

<template>
  <div v-for="(item, i) in items" :key="i">
    <div v-if="item.type === 'Named'">Name: {{ item.name }}</div> <!-- ok -->
    <div v-if="item.type === 'Named'">Age: {{ item.<weak_warning descr="Unresolved variable age">age</weak_warning> }}</div> <!-- expect error -->
    <div v-if="item.type === 'Aged'">Name: {{ item.<weak_warning descr="Unresolved variable name">name</weak_warning> }}</div> <!-- expect error -->
    <div v-if="item.type === 'Aged'">Age: {{ item.age }}</div> <!-- ok -->
  </div>
</template>