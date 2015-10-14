package {
public class Super {
  import mypackage.Alert;

  protected var u;
  public static var u2;

  /**
   * foo
   * @return
   */
  public function foo():Alert {
    bar();
    u = 0;
    u2 = 0;
  }

  /**
   * just bar
   */
  protected function bar() {
    bar();
    t = 0;
  }

  /**
   * tttt
   */
  private var t = 0;

  /**
   * this is uu
   */
  public static function uu() {
  }

  /**
   * and v
   */
  public static var v;
}
}
