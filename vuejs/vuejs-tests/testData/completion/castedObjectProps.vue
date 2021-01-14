<template>
  <div>
    {{ post. }}
    {{ callback. }}
  </div>
</template>

<script lang="ts">
import Vue, {PropType} from 'vue';

interface ComplexMessage {
  title: string;
  okMessage: string;
  cancelMessage: string;
}

export default  {
  props: {
    callback: Object as PropType<ComplexMessage>,
    post: Object as () => {
      id: number
      slug: string
      title: string
      subtitle: string
    },
  },
};
</script>