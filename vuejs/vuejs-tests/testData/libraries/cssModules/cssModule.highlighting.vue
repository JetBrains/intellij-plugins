<template>
  <div>
    {{ $style.foo }}
    {{ $style.<weak_warning descr="Unresolved variable glob1">glob1</weak_warning> }}
    {{ $style.la }}
    {{ myStyle.<weak_warning descr="Unresolved variable foo">foo</weak_warning> }}
    {{ myStyle["my-stuff"] }}
    {{ theStyle.the__stuff }}
    {{ <weak_warning descr="Unresolved variable or type noStyle">noStyle</weak_warning>.<weak_warning descr="Unresolved variable foo">foo</weak_warning> }}
  </div>
</template>

<script>
export default {
  name: "cssModule.vue"
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