<template>
  <div v-if="item">
    <p>
      {{ item.kids ? item.descendants + ' comments' : 'No comments yet.' }}
    </p>
  </div>
</template>
<script>
export default {
  name: 'NewComponent',
  props: {
    item: {}
  }
}
</script>