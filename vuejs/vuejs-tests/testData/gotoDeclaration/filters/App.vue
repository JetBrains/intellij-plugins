<template>
  <div :title="12 | localFilter"></div>
  <div :title="12 | globalFilter"></div>
  <div :title="12 | globalReferencedFilter"></div>
  <div :title="12 | globalQualifiedReferencedFilter"></div>
  <div :title="12 | appFilter"></div>
  <div :title="12 | webTypesFilter"></div>
  <div :title="12 | wrongFilter"></div>
</template>

<script>
    import Vue from 'vue';
    import HelloWorld from './components/HelloWorld.vue';

    export default Vue.extend({
      name: 'app',
      components: {
        HW: HelloWorld,
      },
      filters: {
        localFilter: function (arg1, arg2, arg3) { return true }
      }
  });
</script>
