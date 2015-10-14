package test {

import com.foo.Ambiguous;
import com.foo.MyClass;

public class Test {
    var p1 = new com.foo.MyClass();
    var p2 = new com.foo.MyClass2();
    var p3 = new com.foo.Ambiguous();
}

}