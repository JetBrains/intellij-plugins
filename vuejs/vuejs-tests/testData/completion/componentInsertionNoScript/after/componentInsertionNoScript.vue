<script>
import Vue from "vue";
import Component from "vue-class-component";
import ShortComponent from "./ShortComponent.vue";

@Component({
  components: {ShortComponent}
})
export default class ComponentInsertionNoScript extends Vue {
}
</script>
<template>
  <ShortComponent
</template>