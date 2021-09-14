<template>
  <div>
    <ImportedComponent :bar="<warning descr="Type string is not assignable to type Number">'a'</warning>" :foo="12"></ImportedComponent>
    <WebTypesComponent :foo="<warning descr="Type {a: number} is not assignable to type Foo">{a:12}</warning>"></WebTypesComponent>
    <WebTypesComponent :foo="{a:'a', b: true}"></WebTypesComponent>
  </div>
</template>
<script>
import ImportedComponent from "./ImportedComponent"
export default {
  components: {ImportedComponent}
}
</script>