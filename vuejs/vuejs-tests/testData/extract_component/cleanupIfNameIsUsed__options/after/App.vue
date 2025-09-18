<template>
  <p>Very first paragraph {{ propWithCamel }}</p>
  <p>Second paragraph {{ propWithCamel }}</p>
</template>
<script>
export default {
  props: {
    propWithCamel: {}
  }
}
</script>
