main(param){
  var foo = param as Foo;
  foo.ba<caret>r;
}

class Foo {
  var bar;
}
