<template>
  <custom v-for="<warning descr="Unused local variable item">item</warning> in items">
    Hello!
  </custom>
</template>
<script>
export default {
  props: ['distract'],
  name: 'custom',
  data: function() {
    return {items:[]};
  }
}
</script>
