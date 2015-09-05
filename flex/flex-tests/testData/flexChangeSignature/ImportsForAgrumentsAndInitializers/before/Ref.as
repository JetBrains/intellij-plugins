package {
class Ref {
  public function name():void {
    var v : From;
    v.foo(0);
    v.foo(0);
  }

  public function zzz():void {
    var v : From;
    v.foo(0);
  }
}
}

class Aux {
  private function xxx():void {
    var v : From;
    v.foo(0);
    v.foo(0);
  }
}