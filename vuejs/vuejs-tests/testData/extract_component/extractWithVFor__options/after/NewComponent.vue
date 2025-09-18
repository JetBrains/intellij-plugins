<script>
export default {
  name: 'NewComponent',
  props: {
    item: {}
  }
}
</script>

<template>
  <span>Text: {{ item }}</span>
</template>