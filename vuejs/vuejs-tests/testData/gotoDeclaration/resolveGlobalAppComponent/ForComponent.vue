<script lang="ts">
import {defineComponent, ref} from 'vue'

export default defineComponent({
  setup() {
      const items = ref([
        {name: 'Item1', value: 'Value 1'},
        { name: 'Item2', value: 'Value 2'},
        { name: 'Item3', value: 'Value 3'},
      ])
    return {
        items
    }
  },
})
</script>

<template>
  <GlobalComponent></GlobalComponent>
  <ul>
    <li v-for="(item, index) in items" :key="index">
      {{ index + 1}}. {{ item.name}} - {{item.value}}
    </li>
  </ul>
</template>

<style scoped>

</style>
