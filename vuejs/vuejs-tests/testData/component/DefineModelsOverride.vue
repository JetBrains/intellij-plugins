<script setup lang="ts">
let modelValue = defineModel<string>()
let modelValueOneMoreTime = defineModel<number>({required: true})

let count = defineModel<number>('count')
let countLocal = defineModel<string>('count', {local: true})
let countNonLocalButRequired = defineModel<number | boolean[]>('count', {required: true})
</script>

<template></template>