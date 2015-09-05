package foo {
import zzz.*;

use namespace MyNs;
use namespace ZzNs;
use namespace ZzNs2;

public class From {
  MyNs static function foo() {
    bar();
  }

  MyNs static function bar() {
    Helper.help();
    Helper.help2();
  }
}
}