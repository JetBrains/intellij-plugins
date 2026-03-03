<script lang="ts">
import Vue from "vue";
import ToBeImported from "./toImport.vue";

export default Vue.extend({
  components: {ToBeImported}
})
</script>
<template>
  <to-be-imported
</template>