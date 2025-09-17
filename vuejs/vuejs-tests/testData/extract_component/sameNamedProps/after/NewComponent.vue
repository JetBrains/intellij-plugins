<template>
  <p>Very first paragraph {{ propWithCamel }}</p>
  <p>Second paragraph {{ propWithCamel }}</p>
</template>
<script>
export default {
  name: 'NewComponent',
  props: {
    propWithCamel: {}
  }
}
</script>