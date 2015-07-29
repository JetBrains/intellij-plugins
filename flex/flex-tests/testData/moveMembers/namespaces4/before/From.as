package {
use namespace MyNs;
use namespace MyNs2;
public class From {
  public static function foo() {
    bar();
  }

  MyNs static function bar() {
    ZZ = 0;
    ZZ2 = 0;
  }

  MyNs static const ZZ;
  MyNs2 static const ZZ2;
}
}