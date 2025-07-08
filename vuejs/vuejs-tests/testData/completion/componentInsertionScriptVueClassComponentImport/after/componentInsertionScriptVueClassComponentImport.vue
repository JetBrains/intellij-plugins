<template>
  <ShortComponent
</template>
<script>
import Component from "vue-class-component";
import Vue from "vue";
import ShortComponent from "./ShortComponent.vue";

@Component({
  components: {ShortComponent}
})
export default class ComponentInsertionScriptVueClassComponentImport extends Vue {
}

</script>