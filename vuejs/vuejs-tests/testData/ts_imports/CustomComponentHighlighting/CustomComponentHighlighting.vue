<template>
 <div>
    <<symbolName descr="Custom tag name">CustomComponentHighlighting_2</symbolName> />
    <<symbolName descr="Custom tag name">Button</symbolName> />
    <UnknownTag />
    <<symbolName descr="Custom tag name">CustomComponentHighlighting_2</symbolName>>
      hello
      <div>test</div>
    </<symbolName descr="Custom tag name">CustomComponentHighlighting_2</symbolName>>
 </div>
</template>

<script>
  import CustomComponentHighlighting_2 <symbolName descr="null">from</symbolName> './CustomComponentHighlighting_2.vue'
  import Button <symbolName descr="null">from</symbolName> './Button.vue'
  export default {
    name: 'CustomComponentHighlighting',
    components: {
      CustomComponentHighlighting_2,
      Button
    }
  }
</script>  