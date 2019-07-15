<template>

</template>

<script>
  import decoratedMixin from "./decoratedMixin"
  import regularMixin from "./regularMixin";

  @Component({
               mixins: [
                 regularMixin
               ]
             })
  export default class extends mixins(decoratedMixin) {
    @Prop() componentProp
  }
</script>
