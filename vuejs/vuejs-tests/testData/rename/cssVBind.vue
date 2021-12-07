<template>
  {{color}}
</template>
<script>
export default {
  data() {
    return {
      co<caret>lor: 'red'
    }
  },
  props: {
    bar: Boolean
  }
}
</script>

<style>
.text {
  color: v-bind(color);
}
.div {
  color: v-binds(color);
}
</style>
