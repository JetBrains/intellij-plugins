package {
public class A {
  protected var i : int;
}
}

class B<caret> extends A {
  public function foo() {
    i++;
  }
}