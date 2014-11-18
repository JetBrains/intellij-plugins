<error descr="The part-of directive must be the only directive in a part"><info textAttributesKey="DART_KEYWORD">library</info></error> library;

<error descr="The part-of directive must be the only directive in a part"><info textAttributesKey="DART_KEYWORD">import</info></error> <error descr="Target of URI does not exist: 'foo'">"foo"</error> <info textAttributesKey="DART_KEYWORD">as</info> as <info textAttributesKey="DART_KEYWORD">hide</info> hide <info textAttributesKey="DART_KEYWORD">show</info> show;
<error descr="The part-of directive must be the only directive in a part"><info textAttributesKey="DART_KEYWORD">import</info></error> <error descr="Target of URI does not exist: 'foo'">"foo"</error> <info textAttributesKey="DART_KEYWORD">deferred</info> <info textAttributesKey="DART_KEYWORD">as</info> deferred;
<error descr="The part-of directive must be the only directive in a part"><info textAttributesKey="DART_KEYWORD">export</info></error> <error descr="Target of URI does not exist: 'bar'">"bar"</error> <info textAttributesKey="DART_KEYWORD">show</info> hide <info textAttributesKey="DART_KEYWORD">hide</info> show;

<error descr="The part-of directive must be the only directive in a part"><info textAttributesKey="DART_KEYWORD">part</info></error> <error descr="Target of URI does not exist: 'part'">"part"</error>;
<info textAttributesKey="DART_KEYWORD">part</info> <info textAttributesKey="DART_KEYWORD">of</info> part.of;

<info textAttributesKey="DART_KEYWORD">typedef</info> <error descr="The built-in identifier 'typedef' cannot be used as a type alias name"><info>typedef</info></error>();

<info textAttributesKey="DART_KEYWORD">abstract</info> class <info textAttributesKey="DART_CLASS">Functions</info> <info textAttributesKey="DART_KEYWORD">implements</info> <error descr="Classes can only implement other classes">implements</error> {
  <info textAttributesKey="DART_KEYWORD">factory</info> <error descr="Instance members cannot be accessed from a factory constructor"><error descr="The name of the immediately enclosing class expected"><info>factory</info></error></error>() {}
  <info textAttributesKey="DART_KEYWORD">operator</info> +(<info>a</info>){}

  <info>bool</info> <info>abstract</info>() => false;
  <info textAttributesKey="DART_METADATA">@<info>Object</info></info>() void <info>as</info>() => null;
  void <info>deferred</info>() => null;
  void <info>export</info>() => null;
  <info textAttributesKey="DART_KEYWORD">external</info> void <info>external</info>();
  void <info>factory</info>() => null;
  <info textAttributesKey="DART_KEYWORD">get</info> <info>get</info> => null;
  void <info>implements</info>() => null;
  void <info>import</info>() => null;
  void <info>library</info>() => null;
  void <info>operator</info>() => null;
  void <info>part</info>() => null;
  <info textAttributesKey="DART_KEYWORD">set</info> <info>set</info>(<info>a</info>) => null;
  <info textAttributesKey="DART_KEYWORD">static</info> void <info>static</info>() => null;
  void <info>typedef</info>() => null;

  void <info>on</info>() => null;
  void <info>of</info>() => null;
  void <info>show</info>() => null;
  void <info>hide</info>() => null;

  <info>callFunctions</info>() {
    <info>print</info>(<info>abstract</info>());
    <info>print</info>(as());
    <info>print</info>(deferred());
    <info>print</info>(<info>export</info>());
    <info>print</info>(<info>external</info>());
    <info>print</info>(<info>factory</info>());
    <info>print</info>(<info>get</info>());
    <info>print</info>(<info>implements</info>());
    <info>print</info>(<info>import</info>());
    <info>print</info>(<info>library</info>());
    <info>print</info>(<info>operator</info>());
    <info>print</info>(<info>part</info>());
    <info>print</info>(set());
    <info>print</info>(<info>static</info>());
    <info>print</info>(<info>typedef</info>());

    <info>print</info>(<info>on</info>());
    <info>print</info>(<info>of</info>());
    <info>print</info>(<info>show</info>());
    <info>print</info>(<info>hide</info>());

    var <info textAttributesKey="DART_LOCAL_VARIABLE">async</info>;
    var <info textAttributesKey="DART_LOCAL_VARIABLE">sync</info>;
    <info textAttributesKey="DART_LOCAL_VARIABLE_ACCESS">sync</info>*1;
    <info textAttributesKey="DART_LOCAL_VARIABLE_ACCESS">async</info>*1;
  }

  <info textAttributesKey="DART_INSTANCE_MEMBER_FUNCTION">bar1</info>() <error descr="The modifier 'sync' must be followed by a star ('*')"><info textAttributesKey="DART_KEYWORD">sync</info></error> {}
  <info textAttributesKey="DART_INSTANCE_MEMBER_FUNCTION">bar2</info>() <info textAttributesKey="DART_KEYWORD">sync</info><info textAttributesKey="DART_KEYWORD">*</info> {}
  <info textAttributesKey="DART_INSTANCE_MEMBER_FUNCTION">bar3</info>() <info textAttributesKey="DART_KEYWORD">async</info> {}
  <info textAttributesKey="DART_INSTANCE_MEMBER_FUNCTION">bar4</info>() <info textAttributesKey="DART_KEYWORD">async</info><info textAttributesKey="DART_KEYWORD">*</info> {}
}

<info>main</info>() {
  try{} <info textAttributesKey="DART_KEYWORD">on</info> <error descr="Local variables cannot be referenced before they are declared">on</error>{}

  <info>int</info> <info>abstract</info> = 1;
  <info textAttributesKey="DART_METADATA">@<info>Object</info></info>() var <info>as</info> = 2;
  <info>dynamic</info> <info>deferred</info> = 2.5;
  <info>dynamic</info> <info>export</info> = 3;
  var <info>external</info> = 4;
  var <info>factory</info> = 5;
  var <info>get</info> = 6;
  var <info>implements</info> = 7;
  var <info>import</info> = 8;
  var <info>library</info> = 9;
  var <info>operator</info> = 10;
  var <info>part</info> = 11;
  var <info>set</info> = 12;
  var <info>static</info> = 13;
  var <info>typedef</info> = 14;

  var <info>on</info> = 15;
  var <info>of</info> = 16;
  var <info>show</info> = 17;
  var <info>hide</info> = 18;
  var <info>native</info> = 19;

  <info>print</info>(<info>abstract</info> + 1);
  <info>print</info>(<info>as</info> + 1);
  <info>print</info>(<info>deferred</info> + 1);
  <info>print</info>(<info>export</info> + 1);
  <info>print</info>(<info>external</info> + 1);
  <info>print</info>(<info>factory</info> + 1);
  <info>print</info>(<info>get</info> + 1);
  <info>print</info>(<info>implements</info> + 1);
  <info>print</info>(<info>import</info> + 1);
  <info>print</info>(<info>library</info> + 1);
  <info>print</info>(<info>operator</info> + 1);
  <info>print</info>(<info>part</info> + 1);
  <info>print</info>(<info>set</info> + 1);
  <info>print</info>(<info>static</info> + 1);
  <info>print</info>(<info>typedef</info> + 1);

  <info>print</info>(<info>on</info> + 1);
  <info>print</info>(<info>of</info> + 1);
  <info>print</info>(<info>show</info> + 1);
  <info>print</info>(<info>hide</info> + 1);
  <info>print</info>(<info>native</info> + 1);

  new <info>Functions</info>().<info>callFunctions</info>();
}

enum <info textAttributesKey="DART_CLASS">Foo</info> { <info textAttributesKey="DART_INSTANCE_MEMBER_VARIABLE">BAR</info> }
