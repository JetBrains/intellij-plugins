<script lang="ts">
import type {PropType} from "vue";

const TextAlignConst = ['center', 'left', 'right'] as const;
type TextAlignType = typeof TextAlignConst[number];

export default {
  name: 'NewComponent',
  props: {
    align1: {
      required: true,
      type: String as PropType<TextAlignType>,
    },
    align2: {
      required: false,
      type: String as PropType<TextAlignType>,
    },
    align3: {
      default: "left",
      required: true,
      type: String as PropType<TextAlignType>,
    },
    align4: {
      default: "left",
      required: false,
      type: String as PropType<TextAlignType>,
    },
    align5: {
      default: undefined,
      required: true,
      type: String as PropType<TextAlignType>,
    },
    align6: {
      default: undefined,
      required: false,
      type: String as PropType<TextAlignType>,
    },
    align7: {
      default: () => {
        return undefined;
      },
      type: String as PropType<TextAlignType>,
    },
    align8: {
      default: () => {
        return "left";
      },
      type: String as PropType<TextAlignType>,
    },
  }
}
</script>

<template>
  <div>{{ align1 }}</div>
  <div>{{ align2 }}</div>
  <div>{{ align3 }}</div>
  <div>{{ align4 }}</div>
  <div>{{ align5 }}</div>
  <div>{{ align6 }}</div>
  <div>{{ align7 }}</div>
  <div>{{ align8 }}</div>
</template>