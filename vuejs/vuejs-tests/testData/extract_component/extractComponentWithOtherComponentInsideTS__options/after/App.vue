<template>
  <NewComponent :prop="prop"/>
</template>
<script lang="ts">
import NewComponent from "./NewComponent.vue";

export default {
  name: 'current-comp',
  components: {NewComponent},
  props: ['prop']
}
</script>