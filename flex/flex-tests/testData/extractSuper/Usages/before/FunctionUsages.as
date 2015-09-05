package {
public class FunctionUsages {
    public function foo():SourceClass {
        return null;
    }

    public function foo2():SourceClass {
        return foo();
    }

    public function foo3():SourceClass {
        var t : SourceClass = foo2();
        return foo3();
    }

    public function foo4():SourceClass {
        var t : SourceClass = foo4();
        t.movedMethod();
    }

    public function foo5():SourceClass {
        var t : SourceClass = foo5();
        t.notMovedMethod();
    }

    public function foo6():SourceClass {
        var t : SourceClass = foo6();
        t.notMovedProp();
    }

    public function foo7():SourceClass {
        var t : * = foo7();
        var t2 : Object = foo7();
        var t3 = foo7();
        return null;
    }

    public function foo8():SourceClass {
        var t : UnchangeableUsages = foo8();
    }

    public function foo9():SourceClass {
        foo9().movedProp = 0;
        return null;
    }

    public function foo10():SourceClass {
        trace(foo10().notMovedProp);
        return null;
    }


    public function foo11(i: int, p:IMoved):SourceClass {
        foo11(0, foo11(0, null));
    }

    public function foo12(i: int, p:INotMoved):SourceClass {
        foo12(0, foo12(0, null));
    }
}
}