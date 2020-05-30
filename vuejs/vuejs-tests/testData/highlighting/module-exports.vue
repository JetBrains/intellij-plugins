<template>
  <div class="hello">
    <input type="number" :value="app.<weak_warning descr="Unresolved variable num">num</weak_warning>" @blur="<weak_warning descr="Unresolved variable or type setValue">setValue</weak_warning>" />
    <div>
      <button class="btn" @click="add">+</button>
      <button class="btn" @click="<weak_warning descr="Unresolved variable or type less">less</weak_warning>">-</button>
    </div>
    <p>{{`Param ${msg}`}}</p>
  </div>
</template>
<script>
import {mapState} from "vuex";

<weak_warning descr="Unresolved variable or type module">module</weak_warning>.exports = <weak_warning descr="Unresolved variable or type Vue">Vue</weak_warning>.extend(<weak_warning descr="Argument type {computed: {[p: string]: any}, methods: {add()}, name: string, props: {msg: StringConstructor}} is not assignable to parameter type Node">{</weak_warning>
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