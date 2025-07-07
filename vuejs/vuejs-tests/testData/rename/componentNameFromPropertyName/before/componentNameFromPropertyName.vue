<template>
  <div>
    <BeforeComponent/>
    <BeforeComponent></BeforeComponent>

    <before-component></before-component>
    <before-component/>
  </div>
</template>

<script>
  import c from './componentNameFromDeclaration1'

  export default {
    name: "componentNameFromDeclaration2",
    components: {Before<caret>Component: c}
  }
</script>

<style scoped>

</style>