<info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">globalFun</info>() {
  var <info textAttributesKey="DART_LOCAL_VARIABLE">local1</info>, <info textAttributesKey="DART_LOCAL_VARIABLE">local2</info>;
  <info textAttributesKey="DART_LOCAL_VARIABLE_ACCESS">local1</info> + <info textAttributesKey="DART_LOCAL_VARIABLE_ACCESS">local2</info>;
}

var <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">global1</info>, <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">global2</info>;

class <info textAttributesKey="DART_CLASS">Foo</info>{
  <info textAttributesKey="DART_KEYWORD">static</info> var <info textAttributesKey="DART_STATIC_MEMBER_VARIABLE">staticField1</info>, <info textAttributesKey="DART_STATIC_MEMBER_VARIABLE">staticField2</info>;
  var <info textAttributesKey="DART_INSTANCE_MEMBER_VARIABLE">field1</info>, <info textAttributesKey="DART_INSTANCE_MEMBER_VARIABLE">field2</info>;

  <info textAttributesKey="DART_INSTANCE_MEMBER_FUNCTION"><info textAttributesKey="DART_INSTANCE_MEMBER_FUNCTION">foo</info></info>() {
    var <info textAttributesKey="DART_LOCAL_VARIABLE">local1</info>, <info textAttributesKey="DART_LOCAL_VARIABLE">local2</info>;

    <info textAttributesKey="DART_FUNCTION">inner</info>() {
      var <info textAttributesKey="DART_LOCAL_VARIABLE">localInner1</info>, <info textAttributesKey="DART_LOCAL_VARIABLE">localInner2</info>;
      <info textAttributesKey="DART_LOCAL_VARIABLE_ACCESS">localInner1</info> + <info textAttributesKey="DART_LOCAL_VARIABLE_ACCESS">localInner2</info>;
      <info textAttributesKey="DART_LOCAL_VARIABLE_ACCESS">local1</info> + <info textAttributesKey="DART_LOCAL_VARIABLE_ACCESS">local2</info>;
      <info textAttributesKey="DART_INSTANCE_MEMBER_VARIABLE_ACCESS">field1</info> + <info textAttributesKey="DART_INSTANCE_MEMBER_VARIABLE_ACCESS">field2</info>;
      <info textAttributesKey="DART_STATIC_MEMBER_VARIABLE_ACCESS">staticField1</info> + <info textAttributesKey="DART_STATIC_MEMBER_VARIABLE_ACCESS">staticField2</info>;
      <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_ACCESS">global1</info> + <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_ACCESS">global2</info>;
    }
  }
}
