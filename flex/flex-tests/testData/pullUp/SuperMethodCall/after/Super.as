package {
public class Super extends GrandSuper {
  public function superMethod() {
  }

  public static function superMethodStatic() {
  }

    public function foo() {
        Sub.subMethodStatic();
        superMethod();
        superMethodStatic();
        superMethodStatic();
        grandSuperMethod();
        grandSuperMethodStatic();
        grandSuperMethodStatic();
    }

    override public function abc() {
    }
}
}