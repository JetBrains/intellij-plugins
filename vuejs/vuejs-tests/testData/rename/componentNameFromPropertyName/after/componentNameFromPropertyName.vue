<template>
  <div>
    <AfterComponent/>
    <AfterComponent></AfterComponent>

    <after-component></after-component>
    <after-component/>
  </div>
</template>

<script>
  import c from './componentNameFromDeclaration1'

  export default {
    name: "componentNameFromDeclaration2",
    components: {AfterComponent: c}
  }
</script>

<style scoped>

</style>