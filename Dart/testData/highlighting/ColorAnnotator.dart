<error descr="Annotation can be only constant variable or constant constructor invocation"><info textAttributesKey="DART_METADATA">@aaa.bbb</info>("")</error>
<info textAttributesKey="DART_METADATA">@<info>deprecated</info></info>
<info>foo</info>(){}

<info textAttributesKey="DART_BUILTIN">int</info> <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">x</info>;

<info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">main</info>() {
    <info>int</info> <info>z</info> = <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_CALL">blah</info>();
}

<info>int</info> <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">blah</info>() => <info>x</info>;

class <info textAttributesKey="DART_CLASS">A</info> {
  <info textAttributesKey="DART_KEYWORD">static</info> <info>int</info> <info textAttributesKey="DART_STATIC_MEMBER_VARIABLE">BAR</info> = 4;
  <info textAttributesKey="DART_KEYWORD">static</info> <info textAttributesKey="DART_STATIC_MEMBER_FUNCTION">a</info>() {}
  <info textAttributesKey="DART_INSTANCE_MEMBER_FUNCTION">b</info>() {
    var <info textAttributesKey="DART_LOCAL_VARIABLE">loc</info> =
      <info textAttributesKey="DART_STATIC_MEMBER_FUNCTION_CALL">a</info>() +
      <info textAttributesKey="DART_INSTANCE_MEMBER_VARIABLE_ACCESS">_c</info> +
      <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_ACCESS">x</info>;
    var <info>xx</info> = new <info textAttributesKey="DART_CONSTRUCTOR_CALL"><info>A</info></info>().<info textAttributesKey="DART_INSTANCE_MEMBER_FUNCTION_CALL">b</info>();
  }

  var <info textAttributesKey="DART_INSTANCE_MEMBER_VARIABLE">_c</info> = <info textAttributesKey="DART_STATIC_MEMBER_VARIABLE_ACCESS">BAR</info>;
  <info textAttributesKey="DART_CONSTRUCTOR_DECLARATION">A</info>();
  <info textAttributesKey="DART_CONSTRUCTOR_DECLARATION"><info>A</info>.aaa</info>();
}

<info>abstract</info> class <info>B</info> {
  <info>templateMethod</info>();
  <info>f</info>() => <info textAttributesKey="DART_ABSTRACT_MEMBER_FUNCTION_CALL">templateMethod</info>();
}

class <info>C</info> extends <info>B</info> {
  <info>templateMethod</info>() => null;
  <info>c</info>() => <info textAttributesKey="DART_INHERITED_MEMBER_FUNCTION_CALL">f</info>();
}
