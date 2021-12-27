<template>

<ul>

<li v-for="(item, key) in items" :key="key" @click="clicked(item)">{{ item }}</li>

</ul>

</template>

<script setup>
const items = ["apple", "banana", "orange"];
const clicked = (item) => console.log(item);
</script>
