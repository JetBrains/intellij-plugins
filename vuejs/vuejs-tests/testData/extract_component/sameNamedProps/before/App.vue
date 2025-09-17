<template>
  <selection><p>Very first paragraph {{ propWithCamel }}</p>
  <p>Second paragraph {{ propWithCamel }}</p></selection>
</template>
<script>
export default {
  props: {
    propWithCamel: {}
  }
}
</script>
