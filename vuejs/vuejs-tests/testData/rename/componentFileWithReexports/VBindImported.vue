<script setup lang="ts">
import {SomeOtherName} from "./components-reexport.js";

defineProps<{
  someCustomProp?: string
  inputProp?: string
}>()

const places: any[] = []
</script>

<template>
  <div class="c">{{ someCustomProp }}</div>

  <li v-for="place in places">
    <SomeOtherName :place/>
    <some-other-name :place/>
  </li>
</template>