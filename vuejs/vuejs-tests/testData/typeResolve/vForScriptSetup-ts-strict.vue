<script setup lang="ts">
import { Ref } from 'vue'

let nullableArray: string[] | undefined | null
let nullableArrayRef: Ref<string[] | undefined | null>
</script>

<template>
  <div v-for="item1 in nullableArray">{{ item1 }}</div>
  <div v-for="itemByRef1 in nullableArrayRef">{{ itemByRef1 }}</div>

  <div v-for="(item2, item2Ind) in nullableArray">{{ item2, item2Ind }}</div>
  <div v-for="(itemByRef2, itemByRef2Ind) in nullableArrayRef">{{ itemByRef2, itemByRef2Ind }}</div>
</template>
