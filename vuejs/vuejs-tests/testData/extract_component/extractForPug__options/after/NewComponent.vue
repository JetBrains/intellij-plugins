<template lang="pug">
  span Text: {{ item + data1 }}
</template>
<script>
export default {
  name: 'NewComponent',
  props: {
    data1: {},
    item: {}
  }
}
</script>