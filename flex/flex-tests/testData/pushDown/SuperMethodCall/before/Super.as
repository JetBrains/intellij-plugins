package {
public class Super extends GrandSuper {
  public function superMethod() {
  }

  public static function superMethodStatic() {
  }

    public function foo() {
        Sub1.subMethodStatic();
        Sub2.subMethodStatic();
        superMethod();
        superMethodStatic();
        grandSuperMethod();
        grandSuperMethodStatic();
    }
}
}