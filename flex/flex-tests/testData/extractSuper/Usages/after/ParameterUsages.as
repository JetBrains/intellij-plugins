package {
public class ParameterUsages {
    public function ParameterUsages() {
        var v1: ISuper;
        func1(0, v1);

        var v2: ISuper;
        func2(null, null, v2);

        var v3: SourceClass;
        func3(v3);

        var v4: SourceClass;
        func4(v4);

        var v5: ISuper;
        func5(v5);

        var v6: ISuper;
        func6(v6);

        var v7: SourceClass;
        func7(v7);

        var v8: SourceClass;
        func8(v8);

        var v9: ISuper;
        func9(0, 0, v9);

        var v10: SourceClass;
        func10(v10);

        var v11: ISuper;
        func11(v11);

        var v12: SourceClass;
        func12(v12);

        var v13 : SourceClass;
        unexistingFunc(v13);

        var v14: ISuper;
        func13().func14(v14);
    }

    function func1(i:int, p:*) {}
    function func2(i:*,  z: *, p:Object) {}
    function func3(p:int) {}
    function func4(p:VariablesUsages) {}
    function func5(p:ISuper) {}
    function func6(p:IMoved) {}
    function func7(p:INotMoved) {}
    function func8(p:UnexistingClass) {}
    function func9(...rest) {}
    function func10() {}
    function func11(p:ISuper) {
        p.movedMethod();
    }
    function func12(p:SourceClass) {
        p.notMovedProp = 0;
    }

    static function func13():ParameterUsages {
        return null;
    }
    function func14(p:ISuper) {
        trace(p.movedProp);
    }
}
}