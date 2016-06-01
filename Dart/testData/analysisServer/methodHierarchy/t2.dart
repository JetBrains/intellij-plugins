import "t1.dart";

abstract class T2 {
  int m();
}

abstract class C2 implements T1, T2 {
}

class C2a extends C2 {
  int m() => 4;
}
