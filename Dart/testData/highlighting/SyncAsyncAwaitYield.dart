<info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION"><info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">foo1</info></info>() <info textAttributesKey="DART_KEYWORD">sync</info> => 1;
<info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION"><info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">foo2</info></info>() <info textAttributesKey="DART_KEYWORD">async</info> => 1;
<info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION"><info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">foo3</info></info>() <info textAttributesKey="DART_KEYWORD">sync</info><info textAttributesKey="DART_KEYWORD">*</info> => 1;
<info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION"><info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">foo4</info></info>() <info textAttributesKey="DART_KEYWORD">async</info><info textAttributesKey="DART_KEYWORD">*</info> => 1;

<info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION"><info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">foo5</info></info>(){
  yield <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">a</info>;
  yield*<info textAttributesKey="DART_LOCAL_VARIABLE_ACCESS">a</info>*b;
  <info textAttributesKey="DART_KEYWORD">await</info> for(<info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">a</info> in <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">a</info>);
  await <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">c</info>;
}

<info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION"><info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">bar5</info></info>() <info textAttributesKey="DART_KEYWORD">async</info><info textAttributesKey="DART_KEYWORD">*</info> {
  <info textAttributesKey="DART_KEYWORD">yield</info> a;
  <info textAttributesKey="DART_KEYWORD">yield</info><info textAttributesKey="DART_KEYWORD">*</info>a*b;
  <info textAttributesKey="DART_KEYWORD">await</info> for(<info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">a</info> in <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">a</info>);
  <info textAttributesKey="DART_KEYWORD">await</info> for (;;);
  <info textAttributesKey="DART_KEYWORD">await</info> c;
}
