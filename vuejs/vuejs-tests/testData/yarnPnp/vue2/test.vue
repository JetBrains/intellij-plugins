<template>
  <v-badge
    bordered
    color="error"
    icon="mdi-lock"
    foo
    overlap
  ></v-badge>
  <el-button
    type="primary"
    icon="el-icon-delete"
    foo="bar"
  ></el-button>
  <IntelliJDashboard />
  <<warning descr="Unknown html tag FooBar">FooBar</warning>></<warning descr="Unknown html tag FooBar">FooBar</warning>>
</template>

<script lang="ts">
import Vue from "vue"

export default Vue.extend({
  name: "App",
  components: {
    <error descr="Unresolved variable or type IntelliJDashboard">IntelliJDashboard</error>,
  },
})

Vue.<error descr="Unresolved function or method foo()">foo</error>()
</script>