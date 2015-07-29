package {
public class SourceClass {
    function foo() {
        var v : Object;
        var v2 : SourceClass;
        v = v2;

        var v3 : Object;
        var v4 : SourceClass = v3;

        var v5 : int;
        var v6 : SourceClass = v5;

        var v7 : Aux;
        var v8 : SourceClass = v7;

        var v9: UnexistingType;
        var v10: SourceClass = v9;

        var v11: SourceClass;
        var v12: SourceClass = v11;
    }

    function movedMethod() {}
    function notMovedMethod() {}
}
}

class Aux {}