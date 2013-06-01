class A {
  A operator +(B other){
    <caret>
  }
}
class B {
}

main() {
  A a = new A();
  B b = new B();
  var c = a + b;
}