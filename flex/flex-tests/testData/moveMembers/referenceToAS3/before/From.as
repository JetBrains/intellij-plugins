package {
import com.MyClass;
public class From {
  public static function foo() {
    MyClass.staticMethod();
    var t : MyClass;
    var u = MyClass.staticField;
  }
}
}