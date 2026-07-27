<script vapor lang="ts">
const items = ['a', 'b', 'c']
</script>

<template>
  <dl>
    <template v-for="item in items" :key="item">
      <dt>{{ item }}</dt>
      <dd>{{ item.length }}</dd>
    </template>
  </dl>
</template>
