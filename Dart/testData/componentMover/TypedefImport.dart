import "t1.dart"
  as x;
/*
*/
import "t2.dart";

typedef fn(int);<caret>

f(x) => 4;

class M1 {
  int m() => 1;
}
