<info textAttributesKey="DART_ANNOTATION">@aaa.bbb</info>("")
<info textAttributesKey="DART_ANNOTATION">@<info textAttributesKey="DART_TOP_LEVEL_GETTER_REFERENCE">deprecated</info></info>
<info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION"><info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">foo</info></info>(){}

<info textAttributesKey="DART_CLASS">int</info> <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">x</info>;

<info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION"><info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">main</info></info>() {
    <info textAttributesKey="DART_CLASS">int</info> <info textAttributesKey="DART_LOCAL_VARIABLE_DECLARATION">z</info> = <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_REFERENCE">blah</info>();
}

<info textAttributesKey="DART_CLASS">int</info> <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION"><info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">blah</info></info>() => <info textAttributesKey="DART_TOP_LEVEL_GETTER_REFERENCE">x</info>;

class <info textAttributesKey="DART_CLASS">A</info> {
  <info textAttributesKey="DART_KEYWORD">static</info> <info textAttributesKey="DART_CLASS">int</info> <info textAttributesKey="DART_STATIC_FIELD_DECLARATION">BAR</info> = 4;
  <info textAttributesKey="DART_KEYWORD">static</info> <info textAttributesKey="DART_STATIC_METHOD_DECLARATION"><info textAttributesKey="DART_STATIC_METHOD_REFERENCE">a</info></info>() {}
  <info textAttributesKey="DART_INSTANCE_METHOD_DECLARATION"><info textAttributesKey="DART_INSTANCE_METHOD_REFERENCE">b</info></info>() {
    var <info textAttributesKey="DART_LOCAL_VARIABLE_DECLARATION">loc</info> =
      <info textAttributesKey="DART_STATIC_METHOD_REFERENCE">a</info>() +
      <info textAttributesKey="DART_INSTANCE_GETTER_REFERENCE">_c</info> +
      <info textAttributesKey="DART_TOP_LEVEL_GETTER_REFERENCE">x</info>;
    var <info textAttributesKey="DART_LOCAL_VARIABLE_DECLARATION">xx</info> = new <info textAttributesKey="DART_CONSTRUCTOR"><info textAttributesKey="DART_INSTANCE_METHOD_REFERENCE">A</info></info>().<info textAttributesKey="DART_INSTANCE_METHOD_REFERENCE">b</info>();
  }

  var <info textAttributesKey="DART_INSTANCE_FIELD_DECLARATION">_c</info> = <info textAttributesKey="DART_STATIC_GETTER_REFERENCE">BAR</info> ? true <info textAttributesKey="DART_OPERATION_SIGN">:</info> false;
  <info textAttributesKey="DART_CONSTRUCTOR">A</info>();
  <info textAttributesKey="DART_CONSTRUCTOR"><info textAttributesKey="DART_CLASS">A</info>.aaa</info>();
}

<info textAttributesKey="DART_KEYWORD">abstract</info> class <info textAttributesKey="DART_CLASS">B</info> {
  <info textAttributesKey="DART_INSTANCE_METHOD_DECLARATION"><info textAttributesKey="DART_INSTANCE_METHOD_REFERENCE">templateMethod</info></info>();
  <info textAttributesKey="DART_INSTANCE_METHOD_DECLARATION"><info textAttributesKey="DART_INSTANCE_METHOD_REFERENCE">f</info></info>() => <info textAttributesKey="DART_INSTANCE_METHOD_REFERENCE">templateMethod</info>();
}

class <info textAttributesKey="DART_CLASS">C</info> extends <info textAttributesKey="DART_CLASS">B</info> {
  <info textAttributesKey="DART_INSTANCE_METHOD_DECLARATION"><info textAttributesKey="DART_INSTANCE_METHOD_REFERENCE">templateMethod</info></info>() => null;
  <info textAttributesKey="DART_INSTANCE_METHOD_DECLARATION"><info textAttributesKey="DART_INSTANCE_METHOD_REFERENCE">c</info></info>() => <info textAttributesKey="DART_INSTANCE_METHOD_REFERENCE">f</info>();
}

const <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">className</info> = <info textAttributesKey="DART_SYMBOL_LITERAL">#MyClass</info>;
