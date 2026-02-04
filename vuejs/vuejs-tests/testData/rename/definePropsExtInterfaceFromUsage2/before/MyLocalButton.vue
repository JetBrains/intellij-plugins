<script setup lang="ts">
import {MyProps} from "./fooProps"
const props = defineProps<MyProps>()
</script>

<template>
  {{opt<caret>ionsApiWithType}}
  {{props.optionsApiWithType}}
</template>
