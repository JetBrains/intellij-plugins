<<symbolName descr="Custom tag name">template</symbolName>>
 <div>
    <<symbolName descr="Custom tag name">CustomComponentHighlighting_2</symbolName> />
    <<symbolName descr="Custom tag name">Button</symbolName> />
    <UnknownTag />
    <<symbolName descr="Custom tag name">CustomComponentHighlighting_2</symbolName>>
      hello
      <div>test</div>
    </<symbolName descr="Custom tag name">CustomComponentHighlighting_2</symbolName>>
    <script>
      function <info descr="identifiers//local function">foo</info>() {
      }
    </script>
 </div>
</<symbolName descr="Custom tag name">template</symbolName>>

<<symbolName descr="Custom tag name">script</symbolName>>
  import <info descr="identifiers//exported variable">CustomComponentHighlighting_2</info> <info descr="null">from</info> './CustomComponentHighlighting_2.vue'
  import <info descr="identifiers//exported variable">Button</info> <info descr="null">from</info> './Button.vue'
  export default {
    <info descr="instance field">name</info>: 'CustomComponentHighlighting',
    <info descr="instance field">components</info>: {
      <info descr="identifiers//exported variable">CustomComponentHighlighting_2</info>,
      <info descr="identifiers//exported variable">Button</info>
    }
  }
</<symbolName descr="Custom tag name">script</symbolName>>
