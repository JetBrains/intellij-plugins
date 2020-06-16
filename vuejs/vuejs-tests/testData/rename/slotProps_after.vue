<template>
  <div>
    <template v-slot:header="newNa<caret>me">
      {{ newName.foo }}
      <div v-bind:about="newName.aaa"></div>
    </template>
    <template v-slot:header1="headerProps">
      {{ headerProps.bar }}}
      <div v-bind:about="headerProps.bbb"></div>
    </template>
  </div>
</template>
<script>
  export default {
    props: ["foo", "bar"]
  }
</script>
