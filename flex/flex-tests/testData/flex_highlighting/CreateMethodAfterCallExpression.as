package {
public class CreateMethodAfterCallExpression {
    function CreateMethodAfterCallExpression() {
        foo().<error descr="Unresolved function or method bar()">bar</error>(1);
    }

    function foo():Bar {
    }
}
}

class Bar {

}