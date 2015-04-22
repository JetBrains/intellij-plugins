class <info textAttributesKey="DART_CLASS">A</info> {
  <info textAttributesKey="DART_KEYWORD">static</info> <info textAttributesKey="DART_BUILTIN">int</info> <info textAttributesKey="DART_STATIC_MEMBER_VARIABLE">staticField</info>;
  <info textAttributesKey="DART_KEYWORD">static</info> <info textAttributesKey="DART_BUILTIN">int</info> <info textAttributesKey="DART_KEYWORD">get</info> <info textAttributesKey="DART_STATIC_MEMBER_VARIABLE">staticGetter</info> {
    return 42;
  }
  <info textAttributesKey="DART_KEYWORD">static</info> void <info textAttributesKey="DART_KEYWORD">set</info> <info textAttributesKey="DART_STATIC_MEMBER_VARIABLE">staticSetter</info>(<info textAttributesKey="DART_PARAMETER">x</info>) {
  }

  <info textAttributesKey="DART_BUILTIN">int</info> <info textAttributesKey="DART_INSTANCE_MEMBER_VARIABLE">instanceField</info>;
  <info textAttributesKey="DART_BUILTIN">int</info> <info textAttributesKey="DART_KEYWORD">get</info> <info textAttributesKey="DART_INSTANCE_MEMBER_VARIABLE">instanceGetter</info> {
    return 42;
  }
  void <info textAttributesKey="DART_KEYWORD">set</info> <info textAttributesKey="DART_INSTANCE_MEMBER_VARIABLE">instanceSetter</info>(<info textAttributesKey="DART_PARAMETER">x</info>) {
  }

  <info textAttributesKey="DART_INSTANCE_MEMBER_FUNCTION"><info textAttributesKey="DART_INSTANCE_MEMBER_FUNCTION">main</info></info>() {
    <info textAttributesKey="DART_STATIC_MEMBER_VARIABLE_ACCESS">staticField</info> = 5;
    <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_CALL">print</info>(<info textAttributesKey="DART_STATIC_MEMBER_VARIABLE_ACCESS">staticGetter</info>);
    <info textAttributesKey="DART_STATIC_MEMBER_VARIABLE_ACCESS">staticSetter</info> = 42;

    <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_CALL">print</info>(<info textAttributesKey="DART_INSTANCE_MEMBER_VARIABLE_ACCESS">instanceField</info>);
    <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_CALL">print</info>(<info textAttributesKey="DART_INSTANCE_MEMBER_VARIABLE_ACCESS">instanceGetter</info>);
    <info textAttributesKey="DART_INSTANCE_MEMBER_VARIABLE_ACCESS">instanceSetter</info> = 42;

    <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_CALL">print</info>(<info textAttributesKey="DART_TOP_LEVEL_VARIABLE_ACCESS">topField</info>);
    <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_CALL">print</info>(<info textAttributesKey="DART_TOP_LEVEL_VARIABLE_ACCESS">topGetter</info>);
    <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_ACCESS">topSetter</info> = 42;
  }
}

<info textAttributesKey="DART_BUILTIN">int</info> <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">topField</info> = 0;
<info textAttributesKey="DART_BUILTIN">int</info> <info textAttributesKey="DART_KEYWORD">get</info> <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">topGetter</info> {
  return 42;
}
void <info textAttributesKey="DART_KEYWORD">set</info> <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">topSetter</info>(<info textAttributesKey="DART_PARAMETER">x</info>) {
  <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_CALL">print</info>(<info textAttributesKey="DART_PARAMETER">x</info>);
}
