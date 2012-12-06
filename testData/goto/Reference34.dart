class Foo {
  set test(Foo value) {}
}

main() {
  var tmp = new Foo();
  tmp.te<caret>st = 239;;
}