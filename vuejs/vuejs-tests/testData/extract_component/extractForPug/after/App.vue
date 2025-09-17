<template lang="pug">
    div(v-for="item in items")
        NewComponent(:data1="data1" :item="item")
</template>

<script>
import NewComponent from "./NewComponent.vue"

export default {
  name: "test-v-for",
  components: {NewComponent},
  data() {
    return {
      items: [1, 2, 3, 4, 5],
      data1: 2
    }
  }
}
</script>