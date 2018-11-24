package {
public class CreateMethodAfterCallExpression {
    function CreateMethodAfterCallExpression() {
        foo().bar(1);
    }

    function foo():Bar {
    }
}
}

class Bar {

    internal function bar(i:int):void {
        <caret>
    }
}