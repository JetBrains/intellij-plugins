<template>

</template>

<script lang="ts">
  import decoratedMixin from "./decoratedMixin"
  import regularMixin from "./regularMixin"
  import {Component, Mixins, Prop} from "vue-property-descriptor"

  @Component({
               mixins: [
                 regularMixin
               ]
             })
  export default class extends Mixins(decoratedMixin) {
    @Prop() public componentProp
  }
</script>
