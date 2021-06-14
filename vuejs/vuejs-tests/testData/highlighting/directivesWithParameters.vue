<template>
  <div>
    <a href="#"
       v-clipboard:copy='<weak_warning descr="Unresolved variable or type code">code</weak_warning>'
       v-clipboard:copy.foo.bar='<weak_warning descr="Unresolved variable or type code">code</weak_warning>'
       v-<warning descr="Unrecognized Vue directive">unknown</warning>:<warning descr="Unrecognized argument">foo</warning>='<weak_warning descr="Unresolved variable or type bar">bar</weak_warning>'
       <warning descr="Attribute foo:bar is not allowed here"><error descr="Namespace 'foo' is not bound">foo</error>:bar</warning>="test">
    </a>
  </div>
</template>

<script>
export default {
  directives: {
    clipboard: function() {}
  }
}
</script>