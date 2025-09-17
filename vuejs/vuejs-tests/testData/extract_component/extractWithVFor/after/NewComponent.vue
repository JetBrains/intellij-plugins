<template>
  <span>Text: {{ item }}</span>
</template>
<script>
export default {
  name: 'NewComponent',
  props: {
    item: {}
  }
}
</script>