class FieldParameterSyntax {
  FieldParameterSyntax(this.x){}
  FieldParameterSyntax.a(int this.x){}
  FieldParameterSyntax.b(var this.x, int y){}
  FieldParameterSyntax.b(int x, final this.y){}
}

class WithNamedArguments {
  void m1([int foo([int i])]) {}
  void m2([int foo([int i]), int bar([int i])]) {}
  void m3([int foo([int i, int i]), int bar([int i, int i])]) {}

  void test() {
    foo(x, n1:x);
    foo(x, y, n1:x, n2:x);
    foo(x, y, z, n1:x, n2:x, n3:x);
    foo(n1:x);
    foo(n1:x, n2:x);
    foo(n1:x, n2:x, n3:x);
  }

  foo(covariant Foo param(), covariant param2(), covariant()) {}
  bar(covariant, covariant param, covariant Foo param2, covariant final Foo param3) {}
  covariant Foo a, b;
  covariant var c, d;
}