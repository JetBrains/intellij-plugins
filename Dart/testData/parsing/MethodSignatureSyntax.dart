// Test various basic forms of formal parameters.
class MethodSignatureSyntax extends D implements E, F {
  a();
  b(x);
  c(int x);
  d(var x);
  e(final x);

  f(x, y);
  g(var x, y);
  h(final x, y);
  j(var x, var y);
  k(final x, final y);

  l(int x, y);
  m(int x, int y);
}

main(a, b,) {
  main(1, 2,);
}

foo([a = 5,]) {
  foo(a = 5,);
}

foo1(w, {a: 5,}) {
  foo1(3, a: 5,);
}