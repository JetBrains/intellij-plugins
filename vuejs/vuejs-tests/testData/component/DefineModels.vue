<script setup>
let modelValue = defineModel({required: true, type: String})
let count = defineModel('count', {type: Number})
let countLocal = defineModel('countLocal', {local: true, type: Number})
let countRequired = defineModel('countRequired', {required: true, type: [Number, Boolean]})
let countType = defineModel('countType', {required: false, type: String})
let countAny = defineModel('countAny')
</script>

<template></template>