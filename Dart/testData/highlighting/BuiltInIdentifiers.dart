<error descr="The part-of directive must be the only directive in a part"><info descr="highlighted as keyword">library</info></error> library;
<error descr="The part-of directive must be the only directive in a part"><info descr="highlighted as keyword">part</info></error> <error descr="Target of URI does not exist: 'part'">"part"</error>;
<info descr="highlighted as keyword">part</info> <info descr="highlighted as keyword">of</info> part.of;

<error descr="Import directives must preceed part directives"><error descr="The part-of directive must be the only directive in a part"><info descr="highlighted as keyword">import</info></error></error> <error descr="Target of URI does not exist: 'foo'">"foo"</error> <info descr="highlighted as keyword">as</info> as <info descr="highlighted as keyword">hide</info> hide <info descr="highlighted as keyword">show</info> show;
<error descr="Export directives must preceed part directives"><error descr="The part-of directive must be the only directive in a part"><info descr="highlighted as keyword">export</info></error></error> <error descr="Target of URI does not exist: 'bar'">"bar"</error> <info descr="highlighted as keyword">show</info> hide <info descr="highlighted as keyword">hide</info> show;

<info descr="highlighted as keyword">typedef</info> <error descr="The built-in identifier 'typedef' cannot be used as a type alias name"><info descr="null">typedef</info></error>();

<info descr="highlighted as keyword">abstract</info> class <info descr="null">Functions</info> <info descr="highlighted as keyword">implements</info> <error descr="Classes can only implement other classes">implements</error> {
  <info descr="highlighted as keyword">factory</info> <error descr="The name of the immediately enclosing class expected"><info descr="null">factory</info></error>() {}
  <info descr="highlighted as keyword">operator</info> +(<info descr="null">a</info>){}

  <info descr="null">bool</info> <info descr="null">abstract</info>() => false;
  @<info descr="null">Object</info>() void <info descr="null">as</info>() => null;
  void <info descr="null">export</info>() => null;
  <info descr="highlighted as keyword">external</info> void <info descr="null">external</info>();
  void <info descr="null">factory</info>() => null;
  <info descr="highlighted as keyword">get</info> <info descr="null">get</info> => null;
  void <info descr="null">implements</info>() => null;
  void <info descr="null">import</info>() => null;
  void <info descr="null">library</info>() => null;
  void <info descr="null">operator</info>() => null;
  void <info descr="null">part</info>() => null;
  <info descr="highlighted as keyword">set</info> <info descr="null">set</info>(<info descr="null">a</info>) => null;
  <info descr="highlighted as keyword">static</info> void <info descr="null">static</info>() => null;
  void <info descr="null">typedef</info>() => null;

  void <info descr="null">on</info>() => null;
  void <info descr="null">of</info>() => null;
  void <info descr="null">show</info>() => null;
  void <info descr="null">hide</info>() => null;

  <info descr="null">callFunctions</info>() {
    <info descr="null">print</info>(<info descr="null">abstract</info>());
    <info descr="null">print</info>(as());
    <info descr="null">print</info>(<info descr="null">export</info>());
    <info descr="null">print</info>(<info descr="null">external</info>());
    <info descr="null">print</info>(<info descr="null">factory</info>());
    <info descr="null">print</info>(<info descr="null">get</info>());
    <info descr="null">print</info>(<info descr="null">implements</info>());
    <info descr="null">print</info>(<info descr="null">import</info>());
    <info descr="null">print</info>(<info descr="null">library</info>());
    <info descr="null">print</info>(<info descr="null">operator</info>());
    <info descr="null">print</info>(<info descr="null">part</info>());
    <info descr="null">print</info>(set());
    <info descr="null">print</info>(<info descr="null">static</info>());
    <info descr="null">print</info>(<info descr="null">typedef</info>());

    <info descr="null">print</info>(<info descr="null">on</info>());
    <info descr="null">print</info>(<info descr="null">of</info>());
    <info descr="null">print</info>(<info descr="null">show</info>());
    <info descr="null">print</info>(<info descr="null">hide</info>());
  }
}

<info descr="null">main</info>() {
  try{} <info descr="highlighted as keyword">on</info> <error descr="Local variables cannot be referenced before they are declared">on</error>{}
  
  <info descr="null">int</info> <info descr="null">abstract</info> = 1;
  @<info descr="null">Object</info>() var <info descr="null">as</info> = 2;
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
