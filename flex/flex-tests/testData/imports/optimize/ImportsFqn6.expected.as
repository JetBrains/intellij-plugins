package {
import com.a.*;

public class MyClass {

    var a : ClassA;

    function foo() {
        import com.b.ClassA;

        var b : com.b.ClassA;
    }
}
}
