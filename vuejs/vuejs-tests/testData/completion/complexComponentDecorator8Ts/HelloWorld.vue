<template>

</template>

<script lang="ts">
  import decoratedMixin from "./decoratedMixin"
  import regularMixin from "./regularMixin"
  import {Options, Mixins, Prop} from "vue-class-component"

  @Options({
               mixins: [
                 regularMixin
               ]
             })
  export default class extends Mixins(decoratedMixin) {
    @Prop() public componentProp
  }
</script>
