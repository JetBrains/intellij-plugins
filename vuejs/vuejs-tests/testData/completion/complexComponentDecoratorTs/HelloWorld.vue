<template>

</template>

<script lang="ts">
  import decoratedMixin from "./decoratedMixin"
  import regularMixin from "./regularMixin"
  import {Component, mixins, Prop} from "vue-property-descriptor"

  @Component({
               mixins: [
                 regularMixin
               ]
             })
  export default class extends mixins(decoratedMixin) {
    @Prop() public componentProp
  }
</script>
