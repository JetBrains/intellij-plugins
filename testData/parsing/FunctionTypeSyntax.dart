// Test various forms of function type syntax.
class FunctionTypeSyntax {
  Function a;
  static Function b;

  Function c() { }
  static Function d() { }

  e(Function f) { }
  static f(Function f) { }

  // Dart allows C++ style function types in formal
  // parameter lists.
  g(f()) { }
  h(void f()) { }
  j(f(x)) { }
  k(f(x, y)) { }
  l(int f(int x, int y)) { }
  m(int x, int f(x), int y) { }
}