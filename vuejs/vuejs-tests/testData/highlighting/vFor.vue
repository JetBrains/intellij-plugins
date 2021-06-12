<template>
  <ul>
    <li v-for="(item, <warning descr="Unused local variable key">key</warning>) in items">
      {{ item.message }}
    </li>
  </ul>
</template>
<script>
export default {
  name: 'v-for-test',
  data: {
    items: [
      { message: 'Foo' },
      { message: 'Bar' }
    ]
  }
}
</script>