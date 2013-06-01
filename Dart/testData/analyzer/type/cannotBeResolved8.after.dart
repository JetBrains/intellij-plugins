process(x) {}

class Unknown {
  <caret>
}

main() {
  process(Unknown.foo);
}