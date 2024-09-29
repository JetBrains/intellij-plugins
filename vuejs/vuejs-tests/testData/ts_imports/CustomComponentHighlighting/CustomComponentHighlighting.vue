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
      function <symbolName descr="identifiers//local function">foo</symbolName>() {
      }
    </script>
 </div>
</<symbolName descr="Custom tag name">template</symbolName>>

<<symbolName descr="Custom tag name">script</symbolName>>
  import <symbolName descr="identifiers//exported variable">CustomComponentHighlighting_2</symbolName> <info descr="null">from</info> './CustomComponentHighlighting_2.vue'
  import <symbolName descr="identifiers//exported variable">Button</symbolName> <info descr="null">from</info> './Button.vue'
  export default {
    <symbolName descr="instance field">name</symbolName>: 'CustomComponentHighlighting',
    <symbolName descr="instance field">components</symbolName>: {
      <symbolName descr="identifiers//exported variable">CustomComponentHighlighting_2</symbolName>,
      <symbolName descr="identifiers//exported variable">Button</symbolName>
    }
  }
</<symbolName descr="Custom tag name">script</symbolName>>
