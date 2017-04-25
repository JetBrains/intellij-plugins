foo1() <info textAttributesKey="DART_KEYWORD">sync</info> => 1;
foo2() <info textAttributesKey="DART_KEYWORD">async</info> => 1;
foo3() <info textAttributesKey="DART_KEYWORD">sync</info><info textAttributesKey="DART_KEYWORD">*</info> => 1;
foo4() <info textAttributesKey="DART_KEYWORD">async</info><info textAttributesKey="DART_KEYWORD">*</info> => 1;

foo5(){
  yield a;
  yield*a*b;
<info textAttributesKey="DART_KEYWORD">await</info> for(a in a);
await c;
}

bar5() <info textAttributesKey="DART_KEYWORD">async</info><info textAttributesKey="DART_KEYWORD">*</info> {
<info textAttributesKey="DART_KEYWORD">yield</info> a;
<info textAttributesKey="DART_KEYWORD">yield</info><info textAttributesKey="DART_KEYWORD">*</info>a*b;
<info textAttributesKey="DART_KEYWORD">await</info> for(a in a);
<info textAttributesKey="DART_KEYWORD">await</info> for (;;);
<info textAttributesKey="DART_KEYWORD">await</info> c;<caret>
}
