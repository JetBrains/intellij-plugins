<template>
  <button v-bind:class="buttonClass" v-on:click="handleClick">
    <slot></slot>
  </button>
</template>

<script lang="ts">
import {defineComponent} from 'vue';

export default defineComponent({
  props: {
   buttonClass: {
     type: String,
     default: ''
   }
  },
  methods: {
   handleClick(event: Event) {
     this.$emit('click', event)
   }
  }
})
</script>

