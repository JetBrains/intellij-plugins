<template>
  <div class="hello">
    <h1>{{ m<caret>sg }}</h1>
  </div>
</template>

<script lang="js">
import {defineComponent, PropType} from 'vue';

export default defineComponent({
  name: 'HelloWorld',
  props: {
    msg: {
      type: String
    }
  },
  mounted() {
    console.log(this.msg)
  }
});
</script>
