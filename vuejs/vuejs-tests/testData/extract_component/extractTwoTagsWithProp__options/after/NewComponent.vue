<script>
export default {
  name: 'NewComponent',
  props: {
    one: {}
  }
}
</script>

<template>
  <p>Paragraph! {{ one + 1 }}</p>
  <div>And div {{ unresolved }}</div>
</template>