<info textAttributesKey="DART_KEYWORD">library</info> library;

<info textAttributesKey="DART_KEYWORD">import</info> "foo" <info textAttributesKey="DART_KEYWORD">as</info> as <info textAttributesKey="DART_KEYWORD">hide</info> hide <info textAttributesKey="DART_KEYWORD">show</info> show;
<info textAttributesKey="DART_KEYWORD">import</info> "foo" <info textAttributesKey="DART_KEYWORD">deferred</info> <info textAttributesKey="DART_KEYWORD">as</info> deferred;
<info textAttributesKey="DART_KEYWORD">export</info> "bar" <info textAttributesKey="DART_KEYWORD">show</info> hide <info textAttributesKey="DART_KEYWORD">hide</info> show;

<info textAttributesKey="DART_KEYWORD">part</info> "part";
<info textAttributesKey="DART_KEYWORD">part</info> <info textAttributesKey="DART_KEYWORD">of</info> part.of;

<info textAttributesKey="DART_KEYWORD">typedef</info> typedef();

<info textAttributesKey="DART_KEYWORD">abstract</info> class Functions <info textAttributesKey="DART_KEYWORD">implements</info> implements {
<info textAttributesKey="DART_KEYWORD">factory</info> factory() {}
<info textAttributesKey="DART_KEYWORD">operator</info> +(a){}

bool abstract() => false;
@Object() void as() => null;
void deferred() => null;
void export() => null;
<info textAttributesKey="DART_KEYWORD">external</info> void external();
void factory() => null;
<info textAttributesKey="DART_KEYWORD">get</info> get => null;
void implements() => null;
void import() => null;
void library() => null;
void operator() => null;
void part() => null;
<info textAttributesKey="DART_KEYWORD">set</info> set(a) => null;
<info textAttributesKey="DART_KEYWORD">static</info> void static() => null;
void typedef() => null;

void on() => null;
void of() => null;
void show() => null;
void hide() => null;

callFunctions() {
print(abstract());
print(as());
print(deferred());
print(export());
print(external());
print(factory());
print(get());
print(implements());
print(import());
print(library());
print(operator());
print(part());
print(set());
print(static());
print(typedef());

print(on());
print(of());
print(show());
print(hide());

var async;
var sync;
sync*1;
async*1;
}

bar1() <info textAttributesKey="DART_KEYWORD">sync</info> {}
bar2() <info textAttributesKey="DART_KEYWORD">sync</info><info textAttributesKey="DART_KEYWORD">*</info> {}
bar3() <info textAttributesKey="DART_KEYWORD">async</info> {}
bar4() <info textAttributesKey="DART_KEYWORD">async</info><info textAttributesKey="DART_KEYWORD">*</info> {}
}

main() {
try{} <info textAttributesKey="DART_KEYWORD">on</info> on{}

int abstract = 1;
@Object() var as = 2;
dynamic deferred = 2.5;
dynamic export = 3;
var external = 4;
var factory = 5;
var get = 6;
var implements = 7;
var import = 8;
var library = 9;
var operator = 10;
var part = 11;
var set = 12;
var static = 13;
var typedef = 14;

var on = 15;
var of = 16;
var show = 17;
var hide = 18;
var native = 19;

print(abstract + 1);
print(as + 1);
print(deferred + 1);
print(export + 1);
print(external + 1);
print(factory + 1);
print(get + 1);
print(implements + 1);
print(import + 1);
print(library + 1);
print(operator + 1);
print(part + 1);
print(set + 1);
print(static + 1);
print(typedef + 1);

print(on + 1);
print(of + 1);
print(show + 1);
print(hide + 1);
print(native + 1);

new Functions().callFunctions();
}

enum Foo { BAR }
