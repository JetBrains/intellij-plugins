<template>
  <p>Paragraph! {{ one + 1 }}</p>
  <div>And div {{ unresolved }}</div>
</template>
<script>
export default {
  name: 'NewComponent',
  props: {
    one: {}
  }
}
</script>