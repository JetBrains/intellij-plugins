abstract class A {
  somePublicMethod();
  _somePrivateMethod();
  var a;
  int b;
  double d, e;
  final int f = 1;
}

class B implements A {
  <caret>
}