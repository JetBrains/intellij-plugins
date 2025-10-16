<script>
export default {
  props: {
    propWithCamel: {}
  }
}
</script>

<template>
  <selection><p>Very first paragraph {{ propWithCamel }}</p>
  <p>Second paragraph {{ propWithCamel }}</p></selection>
</template>
