<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  inject: {
    message: {
      default: 'default value here'
    }
  }
})
</script>

<template>
  {{ message }}
</template>