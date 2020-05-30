<template>
  <div v-on:click="$emit()" v-bind:about="$slots">
  </div>
</template>
<script>
  export default {

  }
</script>
