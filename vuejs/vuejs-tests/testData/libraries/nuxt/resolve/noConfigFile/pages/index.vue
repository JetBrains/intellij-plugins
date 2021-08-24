<template>
<div>
  <h1>{{ propInRoot}}</h1> <button @click="commitOnRoot">Root Commit</button>
  <h1>{{ propInModule1}}</h1> <button @click="dispatchOnModule">Module Action</button>
  <h1>{{ propInModule2}}</h1> <button @click="commitOnModule">Module Commit</button>
</div>
</template>

<script>
export default {
  name: "index.vue",
  computed: {
    propInRoot() {
      return this.$store.state.propInRoot;
    },
    propInModule1() {
      return this.$store.state.module1.propInModule1;
    },
    propInModule2() {
      return this.$store.state.module2.propInModule2;
    }
  },
  methods: {
    commitOnRoot() {
      this.$store.commit("update")
    },
    dispatchOnModule() {
      this.$store.dispatch("module1/update")
    },
    commitOnModule() {
      this.$store.commit("module2/update")
    },
  },
}
</script>

<style scoped>

</style>
