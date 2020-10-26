<template>
  <div id="app">
    <h1>Counter is {{ value }}</h1>
    <button @click="incrementCounter">Increment</button>
  </div>
</template>

<script>

import {mapMutations, mapState} from "vuex";

export default {
  name: 'App',
  computed: mapState("counter", ["value", "<weak_warning descr="Cannot resolve Vuex state foo">foo</weak_warning>"]),
  methods: mapMutations({
    incrementCounter: "counter/increment"
  })
}
</script>
