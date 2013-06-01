// Test various basic forms of formal parameters.
interface MethodSignatureSyntax extends D, E {
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