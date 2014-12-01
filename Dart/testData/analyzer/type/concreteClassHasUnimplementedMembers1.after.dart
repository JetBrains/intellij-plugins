abstract class Foo {
  int fooA;

  void fooB();
}
abstract class Bar {
  void barA();
}
class A implements Foo, Bar {
  @override
  int fooA() {
    
  }

  @override
  void fooB() {
    
  }

  @override
  void barA() {
    
  }


}

main() {
  new A();
}