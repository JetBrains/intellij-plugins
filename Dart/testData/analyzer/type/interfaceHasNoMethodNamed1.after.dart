class A {
  foo() {
    <caret>
  }
}

main() {
  A a = new A();
  a.foo();
}