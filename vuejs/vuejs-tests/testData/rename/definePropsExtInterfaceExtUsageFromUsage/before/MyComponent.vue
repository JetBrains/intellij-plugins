<script setup lang="ts">
import {MyProps} from "./fooProps"
const props = defineProps<MyProps>()
</script>

<template>
  {{ optionsApiWithType }}
  {{ props.optionsApiWithType }}
</template>
