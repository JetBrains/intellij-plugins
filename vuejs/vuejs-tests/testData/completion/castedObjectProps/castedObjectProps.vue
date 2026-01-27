<template>
  <div>
    {{ post. }}
    {{ callback. }}
    {{ message. }}
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
    message: {
      type: Object as PropType<Pick<ComplexMessage, "title">>,
      required: true
    },
    post: Object as () => {
      id: number
      slug: string
      title: string
      subtitle: string
    },
  },
};
</script>