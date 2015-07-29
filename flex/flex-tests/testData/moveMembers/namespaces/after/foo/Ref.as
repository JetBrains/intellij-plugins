package foo {
import bar.To;

use namespace MyNs;

public class Ref {
  public function Ref() {
    To.foo();
    To.bar();
  }
}
}