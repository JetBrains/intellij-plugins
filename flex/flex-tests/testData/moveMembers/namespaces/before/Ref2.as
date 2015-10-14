package {
import foo.MyNs;
import foo.From;

use namespace MyNs;

public class Ref2 {
  public function Ref2() {
    From.foo();
    From.bar();
  }
}
}