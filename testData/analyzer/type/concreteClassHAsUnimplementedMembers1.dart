interface Foo {
  int fooA;

  void fooB();
}
interface Bar {
  void barA();
}
class A implements Foo, Bar {
}

main() {
  new A();
}