<template>
  <p v-if="one">Paragraph! {{ compMethod }}</p>
</template>
<script>
export default {
  name: 'NewComponent',
  props: {
    compMethod: {},
    one: {}
  }
}
</script>