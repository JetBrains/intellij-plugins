<template>
  <div>
    {{ post.<caret> }}
  </div>
</template>

<script lang="ts">
import Vue from 'vue';

export default  {
  props: {
    post: Object as () => {
      id: number
      slug: string
      title: string
      subtitle: string
    },
  },
};
</script>