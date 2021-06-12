<template>
  <div id="app">
    <div @event="val =<error descr="Expression expected">></error> bar = val"></div>
    {{bar}}
  </div>
</template>
<script>
export default {
  data: ()=>({bar: 'abc'})
}
</script>