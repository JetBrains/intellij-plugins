<script setup lang="ts">
import {ref} from "vue";

const foo2 = ref("12")
const <warning descr="Unused constant foo3">foo3</warning> = ref(12)
</script>

<template>
  <div ref='foo2'></div>
  <div ref='foo'></div>
  {{ $refs.foo2 }}
  {{ $refs.foo }}
</template>
