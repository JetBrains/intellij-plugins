<template>
  <p>Very first paragraph {{ oneMore }}</p>
  <p>Second paragraph {{ oneMore }}</p>
  <p>Third paragraph {{ oneMore() }}</p>
  <p>Fourth paragraph {{ oneMore() }}</p>
</template>
<script>
export default {
  name: 'NewComponent',
  props: {
    oneMore: {type: Function}
  }
}
</script>