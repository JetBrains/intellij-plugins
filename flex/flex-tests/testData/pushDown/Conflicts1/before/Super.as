package {
public class Super {
  public function Super() {
    foo();
  }

  public function p() {}
  protected function pr() {}
  private function prv() {}
  function i() {}

  public function get prop():String {
    v = 0;
  }

  public function foo() {
    v = 0;
    p();
    pr();
    prv();
    i();
    var a : Aux1;
    var b : Aux2;
  }

  public var v;
}
}

class Aux2 {}