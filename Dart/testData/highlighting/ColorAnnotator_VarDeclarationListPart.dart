<info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION"><info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">globalFun</info></info>() {
  var <info textAttributesKey="DART_LOCAL_VARIABLE_DECLARATION">local1</info>, <info textAttributesKey="DART_LOCAL_VARIABLE_DECLARATION">local2</info>;
  <info textAttributesKey="DART_LOCAL_VARIABLE_REFERENCE">local1</info> + <info textAttributesKey="DART_LOCAL_VARIABLE_REFERENCE">local2</info>;
}

var <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">global1</info>, <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">global2</info>;

class <info textAttributesKey="DART_CLASS">Foo</info>{
  <info textAttributesKey="DART_KEYWORD">static</info> var <info textAttributesKey="DART_STATIC_FIELD_DECLARATION">staticField1</info>, <info textAttributesKey="DART_STATIC_FIELD_DECLARATION">staticField2</info>;
  var <info textAttributesKey="DART_INSTANCE_FIELD_DECLARATION">field1</info>, <info textAttributesKey="DART_INSTANCE_FIELD_DECLARATION">field2</info>;

  <info textAttributesKey="DART_INSTANCE_METHOD_DECLARATION"><info textAttributesKey="DART_INSTANCE_METHOD_REFERENCE">foo</info></info>() {
    var <info textAttributesKey="DART_LOCAL_VARIABLE_DECLARATION">local1</info>, <info textAttributesKey="DART_LOCAL_VARIABLE_DECLARATION">local2</info>;

    <info textAttributesKey="DART_LOCAL_FUNCTION_DECLARATION">inner</info>() {
      var <info textAttributesKey="DART_LOCAL_VARIABLE_DECLARATION">localInner1</info>, <info textAttributesKey="DART_LOCAL_VARIABLE_DECLARATION">localInner2</info>;
      <info textAttributesKey="DART_LOCAL_VARIABLE_REFERENCE">localInner1</info> + <info textAttributesKey="DART_LOCAL_VARIABLE_REFERENCE">localInner2</info>;
      <info textAttributesKey="DART_LOCAL_VARIABLE_REFERENCE">local1</info> + <info textAttributesKey="DART_LOCAL_VARIABLE_REFERENCE">local2</info>;
      <info textAttributesKey="DART_INSTANCE_GETTER_REFERENCE">field1</info> + <info textAttributesKey="DART_INSTANCE_GETTER_REFERENCE">field2</info>;
      <info textAttributesKey="DART_STATIC_GETTER_REFERENCE">staticField1</info> + <info textAttributesKey="DART_STATIC_GETTER_REFERENCE">staticField2</info>;
      <info textAttributesKey="DART_TOP_LEVEL_GETTER_REFERENCE">global1</info> + <info textAttributesKey="DART_TOP_LEVEL_GETTER_REFERENCE">global2</info>;
    }
  }
}
