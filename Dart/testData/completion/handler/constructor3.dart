class Foo {
  Foo(String name);

 factory Foo.fromString(String name) => new Foo(name);
}
main(){
  var a = new Foo.f<caret>
}