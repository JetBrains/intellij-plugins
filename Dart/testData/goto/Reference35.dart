class Foo {
  get test => null;
}

main() {
  var tmp = new Foo();
  tmp.te<caret>st = 239;;
}