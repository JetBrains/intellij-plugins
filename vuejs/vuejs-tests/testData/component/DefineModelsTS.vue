<script setup lang="ts">
let modelValue = defineModel<string>({required: true})
let count = defineModel<number>('count')
let countLocal = defineModel<number>('countLocal', {local: true})
let countRequired = defineModel<number | boolean[]>('countRequired', {required: true})
let countType = defineModel('countType', {required: false, type: String})
</script>

<template></template>