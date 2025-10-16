<script>
export default {
  name: "test-v-for",
  data() {
    return {
      items: [1, 2, 3, 4, 5]
    }
  }
}
</script>

<template>
  <div v-for="item in items">
    <caret><span>Text: {{ item }}</span>
  </div>
</template>
