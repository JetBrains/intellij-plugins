<template lang="pug">
    div(v-for="item in items")
        <selection>span Text: {{ item + data1 }}</selection>
</template>

<script>
export default {
  name: "test-v-for",
  data() {
    return {
      items: [1, 2, 3, 4, 5],
      data1: 2
    }
  }
}
</script>