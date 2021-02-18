<template>
  <div>
    {{ $style.foo }}
    {{ $style.glob1 }}
    {{ $style.la }}
    {{ $style.bar }}
    {{ myStyle.foo }}
    {{ myStyle["my-stuff"] }}
    {{ theStyle.the__stuff }}
    {{ noStyle.foo }}
  </div>
</template>

<script>
export default {
  name: "cssModule.vue",
  computed: {
    foo() {
      this.$style.bar = "aaa"
    }
  }
}
</script>

<style module>
  .foo {

  }
  .bar :global .glob1 :local(.la) .glob2 :local .la2 :global(.glob3) .la3 :global {

  }
  .local4 {

  }
</style>

<style module="myStyle">
  .my-stuff {

  }
</style>

<style module="theStyle">
  .the__stuff {

  }
</style>