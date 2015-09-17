main() {
  label:
  null ? false <info textAttributesKey="DART_OPERATION_SIGN">:</info> true;
  switch(1) {
    case 2:
    default:
  }
  new Foo.named(z: 1);
  <info textAttributesKey="DART_SYMBOL_LITERAL">#Foo.named</info>;
}

class Foo {
  var a = {1: 1};
  Foo.named({z : 1}) : a = 1;
  Foo.redirect() : this.named();
}