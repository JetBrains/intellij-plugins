<script vapor>
defineProps({
  item: {},
})
</script>

<template>
  <div v-if="item">
    <p>
      {{ item.kids ? item.descendants + ' comments' : 'No comments yet.' }}
    </p>
  </div>
</template>