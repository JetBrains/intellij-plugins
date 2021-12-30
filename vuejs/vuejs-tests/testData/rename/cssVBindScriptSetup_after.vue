<template>

  {{ newColor }}
</template>

<script setup >

import {ref} from "vue"

let newColor = 12
let foo = ref("test")

</script>

<style>
div {
  color: v-bind(newColor)
}

</style>