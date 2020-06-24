<template>
  <div>
    {{ this["newName"] }}
  </div>
</template>

<script>
  export default {
    name: 'test',
    data() {
      return {
        newName: 10,
      }
    },
  }
</script>