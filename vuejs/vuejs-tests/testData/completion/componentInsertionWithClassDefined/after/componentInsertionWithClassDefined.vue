<template>
  <ShortComponent
</template>
<script>
import Vue from "vue";
import Component from "vue-class-component";
import ShortComponent from "./ShortComponent.vue";
@Component({
   name: "a123",
  components: {ShortComponent}
})
export default class ComponentInsertionWithClassDefined extends Vue {
}
</script>