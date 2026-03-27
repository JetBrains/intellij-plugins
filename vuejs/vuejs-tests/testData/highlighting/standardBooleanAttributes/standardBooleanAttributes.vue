<template>
  <button
    :disabled="disabled"
  >
    Example
  </button>
  <button
    disabled="<warning descr="Wrong attribute value">err</warning>"
  > Example</button>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
                                 props: {
                                   disabled: {
                                     type: Boolean,
                                     required: true
                                   }
                                 }
                               });
</script>
