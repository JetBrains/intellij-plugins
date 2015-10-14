package {
public class Sub1 extends Super {
  public static function subMethodStatic() {}

    public function foo() {
        subMethodStatic();
        Sub2.subMethodStatic();
        superMethod();
        superMethodStatic();
        grandSuperMethod();
        grandSuperMethodStatic();
    }
}
}