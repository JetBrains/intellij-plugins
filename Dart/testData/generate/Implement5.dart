abstract class I {
  get foo() => 239;
  abstract set foo(x);
}

class <caret>Foo extends I {
}