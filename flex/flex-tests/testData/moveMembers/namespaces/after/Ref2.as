package {
import bar.To;

import foo.MyNs;

use namespace MyNs;

public class Ref2 {
  public function Ref2() {
    To.foo();
    To.bar();
  }
}
}