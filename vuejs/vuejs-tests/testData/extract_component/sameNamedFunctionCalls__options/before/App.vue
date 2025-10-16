<script>
export default {
  methods: {
    oneMore() {
    }
  }
}
</script>

<template>
  <selection><p>Very first paragraph {{ oneMore }}</p>
    <p>Second paragraph {{ oneMore }}</p>
    <p>Third paragraph {{ oneMore() }}</p>
    <p>Fourth paragraph {{ oneMore() }}</p></selection>
</template>
