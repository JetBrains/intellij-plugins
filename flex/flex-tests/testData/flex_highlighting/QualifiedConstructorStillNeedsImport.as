package {
public class QualifiedConstructorStillNeedsImport {
    function foo() {
        var t = new com.foo.<error descr="Qualified name is not imported">MyClass</error>();
    }

}
}