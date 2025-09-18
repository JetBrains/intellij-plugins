<template>
  <caret><p v-if="one">Paragraph! {{ compMethod() }}</p>
</template>
<script>
export default {
  props: {
    one: {}
  },
  computed: {
    compMethod() {
    }
  }
}
</script>