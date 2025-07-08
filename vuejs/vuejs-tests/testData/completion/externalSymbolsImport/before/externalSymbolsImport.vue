<template>
  <div class="hello">
    <h1 :style="">{{ msg }}</h1>
    <ul>
      <li v-for="(item, key) in i" :key="key">{{ item }}</li>
    </ul>
    {{  }}
  </div>
</template>

<script lang="ts" setup>
// eslint-disable-next-line no-undef,@typescript-eslint/no-unused-vars

const myProps = defineProps<{
  msg: string
}>();
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
