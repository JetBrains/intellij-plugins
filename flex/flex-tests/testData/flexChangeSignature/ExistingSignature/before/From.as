package {
public class From {
    function foo() {
    }

    function bar() {
        foo(0, <caret>"abc");
    }
}
}