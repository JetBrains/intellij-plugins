package test {

import com.foo.Ambiguous;
import com.foo.MyClass;
import com.foo.MyClass2;

public class Test {
    var p1 = new MyClass();
    var p2 = new MyClass2();
    var p3 = new com.foo.Ambiguous();
}

}