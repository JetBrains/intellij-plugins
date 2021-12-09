<template>
  <div>
    <ImportedComponent :bar="<weak_warning descr="Type string is not assignable to type number">'a'</weak_warning>" :foo="12"></ImportedComponent>
    <WebTypesComponent :foo="<weak_warning descr="Type {a: number} is not assignable to type Foo">{a:12}</weak_warning>"></WebTypesComponent>
    <WebTypesComponent :foo="{a:'a', b: true}"></WebTypesComponent>
  </div>
</template>
<script>
import ImportedComponent from "./ImportedComponent"
export default {
  components: {ImportedComponent}
}
</script>