<template>
  <label>
    <input v-focus v-local-directive v-some-other-directive v-click-outside/>
  </label>
  <client-comp v-imported-directive></client-comp>
  <div    style=""></div>
</template>

<script>
  import importedDirective from './importedDirective'

  let someOtherDirective = {
    render: function() {

    }
  };
  export default {
    name: "client-comp",
    directives: {
      localDirective: {
        // directive definition
        inserted: function (el) {
          el.focus()
        }
      },
      someOtherDirective,
      importedDirective
    }
  }
</script>
