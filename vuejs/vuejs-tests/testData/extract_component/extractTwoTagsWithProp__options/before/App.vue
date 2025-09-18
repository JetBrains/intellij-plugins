<template>
  <selection><p>Paragraph! {{ one + 1 }}</p>
  <div>And div {{ unresolved }}</div></selection>
</template>
<script>
export default {
  name: 'existing',
  props: {
    one: {}
  }
}
</script>