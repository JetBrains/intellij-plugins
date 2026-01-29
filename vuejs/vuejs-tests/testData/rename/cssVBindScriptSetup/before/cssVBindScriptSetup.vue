<template>

  {{count}}
</template>

<script setup >

import {ref} from "vue"

let cou<caret>nt = 12
let foo = ref("test")

</script>

<style>
div {
  color: v-bind(count)
}

</style>