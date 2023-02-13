class FieldParameterSyntax {
  FieldParameterSyntax(this.x){}
  FieldParameterSyntax.a(int this.x){}
  FieldParameterSyntax.b(var this.x, int y){}
  FieldParameterSyntax.b(int x, final this.y){}
}

class WithNamedArguments {
  void m1([int foo([int i])]) {}
  void m2([int foo([int i]), int bar([int i])]) {}
  void m3([int foo([int i, int i])?, int bar([int i, int i])]) {}

  void test() {
    foo(x, n1:x);
    foo(x, y, n1:x, n2:x);
    foo(x, y, z, n1:x, n2:x, n3:x);
    foo(n1:x);
    foo(n1:x, n2:x);
    foo(n1:x, n2:x, n3:x);
    // Named arguments anywhere
    // https://github.com/dart-lang/language/issues/1072
    foo(n1: x, x);
    foo(n1: x, x, n2: x, n3: x);
    foo(n1: x, "string", n2: x, n3: x, 42, (x) {
      something;
    });
  }

  foo(covariant Foo param(), covariant param2(), covariant()) {}
  bar(covariant, covariant param, covariant Foo param2, covariant final Foo param3) {}
  covariant Foo a, b;
  covariant var c, d;

  foo(int Function(int, [int y, required int z]) ) {}
  foo(int, {int y, required int z}) {}

  static covariant late var a;
  static covariant var a;
  static late var a;
  static var a;
  covariant late var a;
  covariant var a;
  late var a;
  var a;
  covariant late final a;
  covariant final a;
  late final a;
  final a;
  covariant late final int a;
  covariant final int a;
  late final int a;
  final int a;
  covariant late int a;
  covariant int a;
  late int a;
  int a;

  // covariant and late are not keywords here
  covariant late;
  covariant a;
  late covariant;
  var late;
  var covariant;
  final late;
  final required;
  final covariant;
}

void Function({required int a}) g() => throw '';
void Function({required int a, required b c}) h() => throw '';

void foo({
  required final Bar foo5,
  @Deprecated('foo') required final Bar foo6,
  required Bar required,
  @Deprecated('foo') int? foo1,
  @Deprecated('foo') required int? foo2,
  required String this.title
  // required @Deprecated('foo') int? foo3,
}) {}
