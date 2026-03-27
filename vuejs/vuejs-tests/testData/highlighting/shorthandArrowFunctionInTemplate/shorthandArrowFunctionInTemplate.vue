<template>
  <div id="app">
    <div @event="val => bar = val"></div>
    {{bar}}
  </div>
</template>
<script>
export default {
  data: () => ({bar: 'abc'}),
  render() {
    return <div>Hello!</div>
  }
}
</script>