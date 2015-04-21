<info textAttributesKey="DART_METADATA">@aaa.bbb</info>("")
<info textAttributesKey="DART_METADATA">@<info textAttributesKey="DART_TOP_LEVEL_VARIABLE_ACCESS">deprecated</info></info>
<info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION"><info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">foo</info></info>(){}

<info textAttributesKey="DART_BUILTIN">int</info> <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">x</info>;

<info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION"><info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">main</info></info>() {
    <info textAttributesKey="DART_BUILTIN">int</info> <info textAttributesKey="DART_LOCAL_VARIABLE">z</info> = <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_CALL">blah</info>();
}

<info textAttributesKey="DART_BUILTIN">int</info> <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION"><info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">blah</info></info>() => <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_ACCESS">x</info>;

class <info textAttributesKey="DART_CLASS">A</info> {
  <info textAttributesKey="DART_KEYWORD">static</info> <info textAttributesKey="DART_BUILTIN">int</info> <info textAttributesKey="DART_STATIC_MEMBER_VARIABLE">BAR</info> = 4;
  <info textAttributesKey="DART_KEYWORD">static</info> <info textAttributesKey="DART_STATIC_MEMBER_FUNCTION"><info textAttributesKey="DART_STATIC_MEMBER_FUNCTION">a</info></info>() {}
  <info textAttributesKey="DART_INSTANCE_MEMBER_FUNCTION"><info textAttributesKey="DART_INSTANCE_MEMBER_FUNCTION">b</info></info>() {
    var <info textAttributesKey="DART_LOCAL_VARIABLE">loc</info> =
      <info textAttributesKey="DART_STATIC_MEMBER_FUNCTION_CALL">a</info>() +
      <info textAttributesKey="DART_INSTANCE_MEMBER_VARIABLE_ACCESS">_c</info> +
      <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_ACCESS">x</info>;
    var <info textAttributesKey="DART_LOCAL_VARIABLE">xx</info> = new <info textAttributesKey="DART_INSTANCE_MEMBER_FUNCTION_CALL"><info textAttributesKey="DART_CONSTRUCTOR_CALL">A</info></info>().<info textAttributesKey="DART_INSTANCE_MEMBER_FUNCTION_CALL">b</info>();
  }

  var <info textAttributesKey="DART_INSTANCE_MEMBER_VARIABLE">_c</info> = <info textAttributesKey="DART_STATIC_MEMBER_VARIABLE_ACCESS">BAR</info> ? true <info textAttributesKey="DART_OPERATION_SIGN">:</info> false;
  <info textAttributesKey="DART_CONSTRUCTOR_DECLARATION">A</info>();
  <info textAttributesKey="DART_CONSTRUCTOR_DECLARATION"><info textAttributesKey="DART_CLASS">A</info>.aaa</info>();
}

<info textAttributesKey="DART_KEYWORD">abstract</info> class <info textAttributesKey="DART_CLASS">B</info> {
  <info textAttributesKey="DART_INSTANCE_MEMBER_FUNCTION"><info textAttributesKey="DART_INSTANCE_MEMBER_FUNCTION">templateMethod</info></info>();
  <info textAttributesKey="DART_INSTANCE_MEMBER_FUNCTION"><info textAttributesKey="DART_INSTANCE_MEMBER_FUNCTION">f</info></info>() => <info textAttributesKey="DART_ABSTRACT_MEMBER_FUNCTION_CALL">templateMethod</info>();
}

class <info textAttributesKey="DART_CLASS">C</info> extends <info textAttributesKey="DART_CLASS">B</info> {
  <info textAttributesKey="DART_INSTANCE_MEMBER_FUNCTION"><info textAttributesKey="DART_INSTANCE_MEMBER_FUNCTION">templateMethod</info></info>() => null;
  <info textAttributesKey="DART_INSTANCE_MEMBER_FUNCTION"><info textAttributesKey="DART_INSTANCE_MEMBER_FUNCTION">c</info></info>() => <info textAttributesKey="DART_INHERITED_MEMBER_FUNCTION_CALL">f</info>();
}

const <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">className</info> = <info textAttributesKey="DART_SYMBOL_LITERAL">#MyClass</info>;
