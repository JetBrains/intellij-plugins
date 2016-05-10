import 'B.dart';

class A {
  B a() {
    B q = new B();
    a().b().c();
    return q;
  }
}
