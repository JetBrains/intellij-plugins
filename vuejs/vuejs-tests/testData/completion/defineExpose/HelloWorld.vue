<script setup lang="ts">
import {ref} from 'vue'

const visible = ref<boolean>(false)

function open() {
  visible.value = true
}

function someWeirdFunc(): boolean {
  console.log("it works")
  return true
}

const oneNum = 1
const twoRefNum = ref(2)

defineExpose(
  {
    open,
    someFunc: someWeirdFunc,
    oneNum,
    twoRefNum,
    visible
  }
)

defineProps<{ msg: string }>()
</script>

<template>
  <h1 v-show="visible">{{ msg }}</h1>
</template>

