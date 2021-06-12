<template>
  <div id="app">
    <camelCase one-two="test" three-four=1></camelCase>
    <camelCase oneTwo="test" three-four=1></camelCase>
  </div>
</template>
<script>
export default {
  name: 'camelCase',
  props: ['oneTwo']
}
</script>