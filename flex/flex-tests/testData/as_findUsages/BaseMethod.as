package {
public class Foo {
    function myMethod() {}
}
}

class Bar extends Foo {
    override function myMeth<caret>od() {
    }
}

new Foo().myMethod();
new Bar().myMethod()