<template>
  <p>from nested: {{ myprop }}</p>
</template>

<script>
export default {
  name: "NestedComp",
  props: {
    myprop: String,
  }
}
</script>

<style scoped>

</style>
