<template>
  <div ref='foo2'></div>
  <div ref='foo'></div>
  {{ $refs.foo2 }}
  {{ $refs.foo }}
</template>

<script setup lang='js'>
import {ref} from "vue";

const foo2 = ref("12")
const foo3 = ref(12)
</script>