class I {
  get foo => null;
  set foo(x){}
}

class <caret>Foo extends I {
  get foo() => 239;
}