<template>
  <div>
    <template v-slot:header="heade<caret>rProps">
      {{ headerProps.foo }}
      <div v-bind:about="headerProps.aaa"></div>
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
