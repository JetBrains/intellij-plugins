package {
public class Sub2 extends Super {
  public static function subMethodStatic() {}

    public function foo() {
        Sub1.subMethodStatic();
        subMethodStatic();
        superMethod();
        superMethodStatic();
        grandSuperMethod();
        grandSuperMethodStatic();
    }
}
}