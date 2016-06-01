import "t1.dart";
import "t2.dart";

class M1 {
  int m() => 1;
}

class X extends C1a with M1 {
  int m() => 5;
}

class Y extends C2a {
}

class Z extends Y {
  int m() => 6;
}

class Z1 extends Z {
}
