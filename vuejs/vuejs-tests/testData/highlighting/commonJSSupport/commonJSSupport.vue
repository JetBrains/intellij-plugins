<template>
  <div class="hello">
    <input type="number" :value="app.num" @blur="<weak_warning descr="Unresolved variable or type setValue">setValue</weak_warning>" />
    <div>
      <button class="btn" @click="add">+</button>
      <button class="btn" @click="<weak_warning descr="Unresolved variable or type less">less</weak_warning>">-</button>
    </div>
    <p>{{`Param ${msg}`}}</p>
  </div>
</template>
<script>
import {mapState} from "vuex";

<weak_warning descr="Unresolved variable or type module">module</weak_warning>.exports = Vue.<weak_warning descr="Unresolved function or method extend()">extend</weak_warning>({
  name: "HelloWorld",
  props: {
    msg: String
  },
  computed: {
    ...mapState({
      app: "<weak_warning descr="Cannot resolve Vuex state app">app</weak_warning>"
    })
  },
  methods: {
    add() {
    }
  }
});
</script>