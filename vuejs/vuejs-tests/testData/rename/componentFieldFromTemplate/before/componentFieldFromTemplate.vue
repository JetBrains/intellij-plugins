<template>
  <div>
    {{test<caret>Item}}
  </div>
</template>

<script>
  export default {
    name: 'test',
    data() {
      return {
        testItem: 10,
      }
    },
  }
</script>