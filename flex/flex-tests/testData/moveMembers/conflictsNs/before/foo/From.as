package foo {

use namespace MyNs;
use namespace MyNs2;

public class From {
  MyNs static function foo() {
    Other.aux2();
  }
  static var v = Other.aux();
}
}