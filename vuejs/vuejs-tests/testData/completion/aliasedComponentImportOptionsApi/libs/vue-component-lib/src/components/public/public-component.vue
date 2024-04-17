<script lang="ts">
import {defineComponent} from 'vue';

export default defineComponent(
  {
    name: 'PublicComponent',
    props: {
      msg: {
        type: String,
        required: true,
      }
    }
  }
);
</script>

<template>

</template>
