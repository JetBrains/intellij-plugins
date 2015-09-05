package {
public class Sub extends Super {
  public static function subMethodStatic() {}
  public function foo() {
    subMethodStatic();
    superMethod();
    superMethodStatic();
    Super.superMethodStatic();
    grandSuperMethod();
    grandSuperMethodStatic();
    GrandSuper.grandSuperMethodStatic();
  }

  override public function abc() {}
}
}