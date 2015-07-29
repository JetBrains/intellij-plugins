package {
import com.foo.MyClass;

public class QualifiedConstructorStillNeedsImport {
    function foo() {
        var t = new com.foo.MyClass();
    }

}
}