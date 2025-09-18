<script>
export default {
  name: 'NewComponent',
  props: {
    compMethod: {},
    one: {}
  }
}
</script>

<template>
  <p v-if="one">Paragraph! {{ compMethod }}</p>
</template>