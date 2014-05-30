<error descr="The part-of directive must be the only directive in a part"><info textAttributesKey="DART_KEYWORD">library</info></error> library;

<error descr="The part-of directive must be the only directive in a part"><info textAttributesKey="DART_KEYWORD">import</info></error> <error descr="Target of URI does not exist: 'foo'">"foo"</error> <info textAttributesKey="DART_KEYWORD">as</info> as <info textAttributesKey="DART_KEYWORD">hide</info> hide <info textAttributesKey="DART_KEYWORD">show</info> show;
<error descr="The part-of directive must be the only directive in a part"><info textAttributesKey="DART_KEYWORD">import</info></error> <error descr="Target of URI does not exist: 'foo'">"foo"</error> <info textAttributesKey="DART_KEYWORD">deferred</info> <info textAttributesKey="DART_KEYWORD">as</info> deferred;
<error descr="The part-of directive must be the only directive in a part"><info textAttributesKey="DART_KEYWORD">export</info></error> <error descr="Target of URI does not exist: 'bar'">"bar"</error> <info textAttributesKey="DART_KEYWORD">show</info> hide <info textAttributesKey="DART_KEYWORD">hide</info> show;

<error descr="The part-of directive must be the only directive in a part"><info textAttributesKey="DART_KEYWORD">part</info></error> <error descr="Target of URI does not exist: 'part'">"part"</error>;
<info textAttributesKey="DART_KEYWORD">part</info> <info textAttributesKey="DART_KEYWORD">of</info> part.of;

<info textAttributesKey="DART_KEYWORD">typedef</info> <error descr="The built-in identifier 'typedef' cannot be used as a type alias name"><info descr="null">typedef</info></error>();

<info textAttributesKey="DART_KEYWORD">abstract</info> class <info descr="null">Functions</info> <info textAttributesKey="DART_KEYWORD">implements</info> <error descr="Classes can only implement other classes">implements</error> {
  <info textAttributesKey="DART_KEYWORD">factory</info> <error descr="The name of the immediately enclosing class expected"><info descr="null">factory</info></error>() {}
  <info textAttributesKey="DART_KEYWORD">operator</info> +(<info descr="null">a</info>){}

  <info descr="null">bool</info> <info descr="null">abstract</info>() => false;
  <info textAttributesKey="DART_METADATA">@<info descr="null">Object</info></info>() void <info descr="null">as</info>() => null;
  void <info descr="null">deferred</info>() => null;
  void <info descr="null">export</info>() => null;
  <info textAttributesKey="DART_KEYWORD">external</info> void <info descr="null">external</info>();
  void <info descr="null">factory</info>() => null;
  <info textAttributesKey="DART_KEYWORD">get</info> <info descr="null">get</info> => null;
  void <info descr="null">implements</info>() => null;
  void <info descr="null">import</info>() => null;
  void <info descr="null">library</info>() => null;
  void <info descr="null">operator</info>() => null;
  void <info descr="null">part</info>() => null;
  <info textAttributesKey="DART_KEYWORD">set</info> <info descr="null">set</info>(<info descr="null">a</info>) => null;
  <info textAttributesKey="DART_KEYWORD">static</info> void <info descr="null">static</info>() => null;
  void <info descr="null">typedef</info>() => null;

  void <info descr="null">on</info>() => null;
  void <info descr="null">of</info>() => null;
  void <info descr="null">show</info>() => null;
  void <info descr="null">hide</info>() => null;

  <info descr="null">callFunctions</info>() {
    <info descr="null">print</info>(<info descr="null">abstract</info>());
    <info descr="null">print</info>(as());
    <info descr="null">print</info>(deferred());
    <info descr="null">print</info>(<info descr="null">export</info>());
    <info descr="null">print</info>(<info descr="null">external</info>());
    <info descr="null">print</info>(<info descr="null">factory</info>());
    <info descr="null">print</info>(get());
    <info descr="null">print</info>(<info descr="null">implements</info>());
    <info descr="null">print</info>(<info descr="null">import</info>());
    <info descr="null">print</info>(<info descr="null">library</info>());
    <info descr="null">print</info>(<info descr="null">operator</info>());
    <info descr="null">print</info>(<info descr="null">part</info>());
    <info descr="null">print</info>(set());
    <info descr="null">print</info>(<info descr="null">static</info>());
    <info descr="null">print</info>(typedef());

    <info descr="null">print</info>(<info descr="null">on</info>());
    <info descr="null">print</info>(<info descr="null">of</info>());
    <info descr="null">print</info>(<info descr="null">show</info>());
    <info descr="null">print</info>(<info descr="null">hide</info>());
  }
}

<info descr="null">main</info>() {
  try{} <info textAttributesKey="DART_KEYWORD">on</info> <error descr="Local variables cannot be referenced before they are declared">on</error>{}
  
  <info descr="null">int</info> <info descr="null">abstract</info> = 1;
  <info textAttributesKey="DART_METADATA">@<info descr="null">Object</info></info>() var <info descr="null">as</info> = 2;
  <info descr="null">dynamic</info> <info descr="null">deferred</info> = 2.5;
  <info descr="null">dynamic</info> <info descr="null">export</info> = 3;
  var <info descr="null">external</info> = 4;
  var <info descr="null">factory</info> = 5;
  var <info descr="null">get</info> = 6;
  var <info descr="null">implements</info> = 7;
  var <info descr="null">import</info> = 8;
  var <info descr="null">library</info> = 9;
  var <info descr="null">operator</info> = 10;
  var <info descr="null">part</info> = 11;
  var <info descr="null">set</info> = 12;
  var <info descr="null">static</info> = 13;
  var <info descr="null">typedef</info> = 14;

  var <info descr="null">on</info> = 15;
  var <info descr="null">of</info> = 16;
  var <info descr="null">show</info> = 17;
  var <info descr="null">hide</info> = 18;
  var <info descr="null">native</info> = 19;

  <info descr="null">print</info>(<info descr="null">abstract</info> + 1);
  <info descr="null">print</info>(<info descr="null">as</info> + 1);
  <info descr="null">print</info>(<info descr="null">deferred</info> + 1);
  <info descr="null">print</info>(<info descr="null">export</info> + 1);
  <info descr="null">print</info>(<info descr="null">external</info> + 1);
  <info descr="null">print</info>(<info descr="null">factory</info> + 1);
  <info descr="null">print</info>(<info descr="null">get</info> + 1);
  <info descr="null">print</info>(<info descr="null">implements</info> + 1);
  <info descr="null">print</info>(<info descr="null">import</info> + 1);
  <info descr="null">print</info>(<info descr="null">library</info> + 1);
  <info descr="null">print</info>(<info descr="null">operator</info> + 1);
  <info descr="null">print</info>(<info descr="null">part</info> + 1);
  <info descr="null">print</info>(<info descr="null">set</info> + 1);
  <info descr="null">print</info>(<info descr="null">static</info> + 1);
  <info descr="null">print</info>(<info descr="null">typedef</info> + 1);

  <info descr="null">print</info>(<info descr="null">on</info> + 1);
  <info descr="null">print</info>(<info descr="null">of</info> + 1);
  <info descr="null">print</info>(<info descr="null">show</info> + 1);
  <info descr="null">print</info>(<info descr="null">hide</info> + 1);
  <info descr="null">print</info>(<info descr="null">native</info> + 1);

  new <info descr="null">Functions</info>().<info descr="null">callFunctions</info>();
}
