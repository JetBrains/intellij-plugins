package bar {
import foo.MyNs;

import zzz.Helper;
import zzz.ZzNs;
import zzz.ZzNs2;

use namespace MyNs;
use namespace ZzNs;
use namespace ZzNs2;

public class To {

  public function To() {
    Helper.help2();
  }

    MyNs static function foo() {
        bar();
    }

    MyNs static function bar() {
        Helper.help();
        Helper.help2();
    }
}
}