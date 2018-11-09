<template>
  <div>
    <BeforeComponent/>
    <BeforeComponent></BeforeComponent>

    <before-component></before-component>
    <before-component/>
  </div>
</template>

<script>
  import BeforeComponent from './componentNameFromDeclaration1'

  export default {
    name: "componentNameFromDeclaration2",
    components: {BeforeComponent}
  }
</script>

<style scoped>

</style>