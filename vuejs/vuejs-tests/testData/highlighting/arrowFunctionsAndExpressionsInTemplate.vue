<template>
  <p>Completed Tasks: {{ ((todo) => todo.done === true)({done: 111}) }}</p>
  <p>Pending Tasks: {{ todos.filter((todo) => {return todo.done === false}).length }}</p>
  <div class="map" v-bind:class="{ 'map--loading': 'test', aaa: 118 }">Additional...</div>
  {{todos}}
</template>
<script>
export default {
  data: () => ({todos: [{done: true}]})
}
</script>