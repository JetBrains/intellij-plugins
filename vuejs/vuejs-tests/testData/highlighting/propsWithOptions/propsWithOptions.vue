<template>
  <!-- test required props -->
  <<warning descr="Element foo doesn't have required attribute dec_bool2"><warning descr="Element foo doesn't have required attribute dec_string2"><warning descr="Element foo doesn't have required attribute model_string1"><warning descr="Element foo doesn't have required attribute reg_bool1"><warning descr="Element foo doesn't have required attribute reg_bool3"><warning descr="Element foo doesn't have required attribute synced_string1">foo</warning></warning></warning></warning></warning></warning>>

  </<warning descr="Element foo doesn't have required attribute dec_bool2"><warning descr="Element foo doesn't have required attribute dec_string2"><warning descr="Element foo doesn't have required attribute model_string1"><warning descr="Element foo doesn't have required attribute reg_bool1"><warning descr="Element foo doesn't have required attribute reg_bool3"><warning descr="Element foo doesn't have required attribute synced_string1">foo</warning></warning></warning></warning></warning></warning>>

  <!-- test boolean props -->
  <foo dec_bool1
       dec_bool2
       dec_bool3
       dec_bool4
       <warning descr="dec_string1 requires value">dec_string1</warning>
       <warning descr="dec_string2 requires value">dec_string2</warning>
       <warning descr="model_string1 requires value">model_string1</warning>
       randomProp
       reg_bool1
       reg_bool2
       reg_bool3
       reg_bool4
       synced_bool1
       <warning descr="synced_string1 requires value">synced_string1</warning>
  ></foo>
</template>
<script>
  import component from "./component";

  export default {
    components: {
      foo: component
    }
  }
</script>
