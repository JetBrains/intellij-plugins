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
  import <info descr="exported variable">CustomComponentHighlighting_2</info> <info descr="null">from</info> './CustomComponentHighlighting_2.vue'
  import <info descr="exported variable">Button</info> <info descr="null">from</info> './Button.vue'
  export default {
    <info descr="instance field">name</info>: 'CustomComponentHighlighting',
    <info descr="instance field">components</info>: {
      <info descr="exported variable">CustomComponentHighlighting_2</info>,
      <info descr="exported variable">Button</info>
    }
  }
</script>