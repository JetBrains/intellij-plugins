class <info textAttributesKey="DART_CLASS">A</info> {
  <info textAttributesKey="DART_KEYWORD">static</info> <info textAttributesKey="DART_CLASS">int</info> <info textAttributesKey="DART_STATIC_FIELD_DECLARATION">staticField</info>;
  <info textAttributesKey="DART_KEYWORD">static</info> <info textAttributesKey="DART_CLASS">int</info> <info textAttributesKey="DART_KEYWORD">get</info> <info textAttributesKey="DART_STATIC_GETTER_DECLARATION">staticGetter</info> {
    return 42;
  }
  <info textAttributesKey="DART_KEYWORD">static</info> void <info textAttributesKey="DART_KEYWORD">set</info> <info textAttributesKey="DART_STATIC_GETTER_DECLARATION">staticSetter</info>(<info textAttributesKey="DART_PARAMETER_DECLARATION">x</info>) {
  }

  <info textAttributesKey="DART_CLASS">int</info> <info textAttributesKey="DART_INSTANCE_FIELD_DECLARATION">instanceField</info>;
  <info textAttributesKey="DART_CLASS">int</info> <info textAttributesKey="DART_KEYWORD">get</info> <info textAttributesKey="DART_INSTANCE_GETTER_DECLARATION">instanceGetter</info> {
    return 42;
  }
  void <info textAttributesKey="DART_KEYWORD">set</info> <info textAttributesKey="DART_INSTANCE_GETTER_DECLARATION">instanceSetter</info>(<info textAttributesKey="DART_PARAMETER_DECLARATION">x</info>) {
  }

  <info textAttributesKey="DART_INSTANCE_METHOD_DECLARATION"><info textAttributesKey="DART_INSTANCE_METHOD_REFERENCE">main</info></info>() {
    <info textAttributesKey="DART_STATIC_GETTER_REFERENCE">staticField</info> = 5;
    <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_REFERENCE">print</info>(<info textAttributesKey="DART_STATIC_GETTER_DECLARATION">staticGetter</info>);
    <info textAttributesKey="DART_STATIC_GETTER_DECLARATION">staticSetter</info> = 42;

    <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_REFERENCE">print</info>(<info textAttributesKey="DART_INSTANCE_GETTER_REFERENCE">instanceField</info>);
    <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_REFERENCE">print</info>(<info textAttributesKey="DART_INSTANCE_GETTER_DECLARATION">instanceGetter</info>);
    <info textAttributesKey="DART_INSTANCE_GETTER_DECLARATION">instanceSetter</info> = 42;

    <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_REFERENCE">print</info>(<info textAttributesKey="DART_TOP_LEVEL_GETTER_REFERENCE">topField</info>);
    <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_REFERENCE">print</info>(<info textAttributesKey="DART_TOP_LEVEL_GETTER_DECLARATION">topGetter</info>);
    <info textAttributesKey="DART_TOP_LEVEL_GETTER_DECLARATION">topSetter</info> = 42;
  }
}

<info textAttributesKey="DART_CLASS">int</info> <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">topField</info> = 0;
<info textAttributesKey="DART_CLASS">int</info> <info textAttributesKey="DART_KEYWORD">get</info> <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">topGetter</info> {
  return 42;
}
void <info textAttributesKey="DART_KEYWORD">set</info> <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">topSetter</info>(<info textAttributesKey="DART_PARAMETER_DECLARATION">x</info>) {
  <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_REFERENCE">print</info>(<info textAttributesKey="DART_PARAMETER_DECLARATION">x</info>);
}
