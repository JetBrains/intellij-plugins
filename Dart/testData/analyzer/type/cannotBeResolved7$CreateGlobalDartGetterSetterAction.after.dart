process(x) {}

get unknown => <caret>;

class A {
  foo() {
    process(unknown);
  }
}