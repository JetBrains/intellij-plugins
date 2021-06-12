<template lang="pug">
  ul
    li(v-for="itemP in itemsP") {{ itemP.message }}
</template>
<script>
export default {
  name: 'v-for-test',
  data: {
    itemsP: [
      { message: 'Foo' },
      { message: 'Bar' }
    ]
  }
}
</script>