import 'B.dart';

class A {
  A() {
    new B().b();
  }
  a() {
    B b = new B();
    b.b();
  }
}
