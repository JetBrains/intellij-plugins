<script>
import NewComponent from "./NewComponent.vue";

export default {
  name: "test-v-for",
  components: {NewComponent},
  data() {
    return {
      items: [1, 2, 3, 4, 5]
    }
  }
}
</script>

<template>
  <div v-for="item in items">
    <NewComponent :item="item"/>
  </div>
</template>
