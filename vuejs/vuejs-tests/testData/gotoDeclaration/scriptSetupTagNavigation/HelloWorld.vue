<template>
  <div class="hello">
    <h1>{{ msg }}</h1>
    <Sample/>
  </div>
</template>

<script setup>
/* eslint-disable */

import Sample from './Sample';

const props= defineProps({
    msg: {
      type: String,
      default: "Hi"
    }
  })

</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h3 {
  margin: 40px 0 0;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
</style>
