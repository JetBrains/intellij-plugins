abstract class Foo {
  int fooA;

  void fooB();
}
abstract class Bar {
  void barA();
}
class A implements Foo, Bar {
}

main() {
  new A();
}