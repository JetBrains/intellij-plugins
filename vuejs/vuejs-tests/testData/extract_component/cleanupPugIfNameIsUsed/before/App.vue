<template lang="pug">
    <caret>div(v-if="items()")
        div(v-for="item in items()")
            p Id: {{ item.id }}, name: {{ item.name }}
</template>
<script>
export default {
  props: {
    items: {}
  }
}
</script>
