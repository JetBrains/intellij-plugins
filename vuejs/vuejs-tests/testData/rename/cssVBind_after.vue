<template>
  {{ newColor }}
</template>
<script>
export default {
  data() {
    return {
      newColor: 'red'
    }
  },
  props: {
    bar: Boolean
  }
}
</script>

<style>
.text {
  color: v-bind(newColor);
}
.div {
  color: v-binds(color);
}
</style>
