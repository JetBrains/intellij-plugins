<template>
  <HelloWorld ref="triggerRef" msg="Hello!"/>
</template>

<script setup lang="ts">
import {ref} from "vue";
import HelloWorld from "./HelloWorld.vue"

const triggerRef = ref<InstanceType<typeof HelloWorld>>()
const result1 = triggerRef.value?.<caret>;
</script>
