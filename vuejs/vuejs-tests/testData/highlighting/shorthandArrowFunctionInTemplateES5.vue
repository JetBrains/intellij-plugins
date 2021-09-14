<template>
  <div id="app">
    <div @event="<weak_warning descr="Unresolved variable or type val">val</weak_warning> =<error descr="Expression expected">></error> bar = val"></div>
    {{bar}}
  </div>
</template>
<script>
export default {
  data: ()=>({bar: 'abc'})
}
</script>