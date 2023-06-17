<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  inject: ['message']
})
</script>

<template>
  {{ message }}
</template>