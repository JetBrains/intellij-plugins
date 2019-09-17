<template>
  <div v-for="vFor in bar">
    <template v-slot:header="headerProps">
      {{ <caret> }}
    </template>
    <div v-for="vFor2 in foo"></div>
    <template v-slot:header1="header1Props"></template>
  </div>
</template>
<script>
  export default {
    props: ["foo", "bar"]
  }
</script>
