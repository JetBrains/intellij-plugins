// Test more details on the formal parameter syntax.
class FormalParameterSyntax {
  a([x = 42]) { }
  b([int x = 42]) { }
  c(x, [y = 42]) { }
  d(x, [int y = 42]) { }
}