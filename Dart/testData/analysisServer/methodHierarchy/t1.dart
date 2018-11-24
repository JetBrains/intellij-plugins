abstract class T1 {
  int m();
}

abstract class C1 implements T1 {
}

class C1a extends C1 {
  int m() => 3;
}
