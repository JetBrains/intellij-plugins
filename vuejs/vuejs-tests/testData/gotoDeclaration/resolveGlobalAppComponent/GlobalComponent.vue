<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "GlobalComponent"
})
</script>

<template>
  <div class="global-component"> I am a global component</div>
</template>

<style scoped>
.global-component {
  color: blue;
}
</style>