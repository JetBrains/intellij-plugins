<info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">foo1</info>() <error descr="The modifier 'sync' is not allowed for an exrpression function body"><info textAttributesKey="DART_KEYWORD">sync</info></error> => 1;
<info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">foo2</info>() <info textAttributesKey="DART_KEYWORD">async</info> => 1;
<info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">foo3</info>() <error descr="The modifier 'sync' is not allowed for an exrpression function body"><info textAttributesKey="DART_KEYWORD">sync</info></error><info textAttributesKey="DART_KEYWORD">*</info> => 1;
<info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">foo4</info>() <info textAttributesKey="DART_KEYWORD">async</info><error descr="The modifier 'async*' is not allowed for an expression function body"><info textAttributesKey="DART_KEYWORD">*</info></error> => 1;

<info>foo5</info>(){
  <warning descr="Undefined class 'yield'">yield</warning> <info>a</info>;
  <warning descr="Undefined name 'yield'">yield</warning>*<info>a</info>*<warning descr="Undefined name 'b'">b</warning>;
  <error descr="The asynchronous for-in can only be used in a function marked with async or async*"><info textAttributesKey="DART_KEYWORD">await</info></error> for(<info>a</info> in <info>a</info>);
  <warning descr="Undefined class 'await'">await</warning> <info>c</info>;
}

<info>bar5</info>() <info textAttributesKey="DART_KEYWORD">async</info><info textAttributesKey="DART_KEYWORD">*</info> {
  <info textAttributesKey="DART_KEYWORD">yield</info> <warning descr="Undefined name 'a'">a</warning>;
  <info textAttributesKey="DART_KEYWORD">yield</info><info textAttributesKey="DART_KEYWORD">*</info><warning descr="Undefined name 'a'">a</warning>*<warning descr="Undefined name 'b'">b</warning>;
  <info textAttributesKey="DART_KEYWORD">await</info> for(<warning descr="Undefined name 'a'"><info>a</info></warning> in <warning descr="Undefined name 'a'"><info>a</info></warning>);
  <error descr="The modifier 'await' is not allowed for a normal 'for' statement"><info textAttributesKey="DART_KEYWORD">await</info></error> for (;;);
  <info textAttributesKey="DART_KEYWORD">await</info> <warning descr="Undefined name 'c'">c</warning>;
}
