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
  n(p1, int p2, p3(), void p4(String p5), [p6 = v1, int p7 = 8, p8() = v2, void p9(String z) = v3, p10, int p11, p12(), void p13(String z)]){}
  o(p1, int p2, p3(), void p4(String p5), {p6 : v1, int p7 : 8, p8() : v2, void p9(String z) : v3, p10, int p11, p12(), void p13(String z)}){}
  p([p1]){}
  q({p2}){}
}