process(x) {}
class A {
  get unknown => <caret>;

  foo() {
    process(unknown);
  }
}