import "t1.dart"
  as x;
/*
*/
import "t2.dart";

class X <caret>extends x.C1a with M1 {
  int m() => 5;
}

class M1 {
  int m() => 1;
}

class Y extends C2a {
  Y() : super();
}

class Z extends Y {
  int m() => new x.C1a().m();
}

class Z1 extends Z {
}
