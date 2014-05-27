<error descr="Annotation can be only constant variable or constant constructor invocation"><info descr="metadata">@aaa.bbb</info>("")</error>
<info descr="metadata">@<info>deprecated</info></info>
<info>foo</info>(){}

<info textAttributesKey="DART_BUILTIN">int</info> <info textAttributesKey="DART_TOP_LEVEL_VARIABLE_DECLARATION">x</info>;

<info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">main</info>() {
    <info>int</info> <info>z</info> = <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_CALL">blah</info>();
}

<info>int</info> <info textAttributesKey="DART_TOP_LEVEL_FUNCTION_DECLARATION">blah</info>() => <info>x</info>;

class <info textAttributesKey="DART_CLASS">A</info> {
  <info textAttributesKey="DART_KEYWORD">static</info> <info>int</info> <info textAttributesKey="DART_STATIC_MEMBER_VARIABLE">BAR</info> = 4;

}