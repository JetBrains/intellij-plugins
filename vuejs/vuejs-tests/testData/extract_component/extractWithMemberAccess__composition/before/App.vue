<script setup>
defineProps({
  item: {},
})
</script>

<template>
  <caret><div v-if="item">
    <p>
      {{ item.kids ? item.descendants + ' comments' : 'No comments yet.' }}
    </p>
  </div>
</template>
