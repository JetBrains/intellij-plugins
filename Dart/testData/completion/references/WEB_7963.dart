library test;
class Foo {

  final int id = 0;
}
void main() {
  var foo = new Foo();
  print( {"id" : (foo.i<caret>d + 1)} );
}
