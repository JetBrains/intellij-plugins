<template>
  <div class="hello">
    <h1 :style="Color.red">{{ msg }}</h1>
    <ul>
      <li v-for="(item, key) in items()<caret>" :key="key">{{ item }}</li>
    </ul>
    {{ getText() }}
  </div>
</template>

<script lang="ts">
// eslint-disable-next-line no-undef,@typescript-eslint/no-unused-vars

import {Color} from "./Color";
import {defineComponent} from "vue";
import {getText, items} from "./Items";

export default defineComponent({
  methods: {
    items() {
      return items
    }, getText},
  computed: {
    Color() {
      return Color
    }
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
