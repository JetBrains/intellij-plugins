<template>
  <div>
    <AfterComponent/>
    <AfterComponent></AfterComponent>

    <after-component></after-component>
    <after-component/>
  </div>
</template>

<script>
  import AfterComponent from './componentNameFromDeclaration1'

  export default {
    name: "componentNameFromDeclaration2",
    components: {AfterComponent}
  }
</script>

<style scoped>

</style>