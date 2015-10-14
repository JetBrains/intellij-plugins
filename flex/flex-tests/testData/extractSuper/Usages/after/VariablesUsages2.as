package {
public class VariablesUsages2 {
    public function test() {
        var v1: SourceClass;
        v1 = unexistingVariable;

        var v2 : ISuper;
        v2 = null;

        var v3: SourceClass;
        v3 = 0;

        var v4: ISuper;
        v4 = new SourceClass();

        var v5: ISuper;
        v5 = new SourceClassEx();

        var v6: SourceClass;
        v6 = new VariablesUsages2();

        var v7: ISuper;
        var v8: * = v7;

        var v7_: ISuper;
        var v8_: Object = v7_;

        var v9: ISuper;
        var v10 = v9;

        var v11: SourceClass;
        var v12:int = v11;

        var v13: SourceClass;
        var v14:UnexistingType = v13;

        var v15: SourceClass;
        var v16:VariablesUsages2 = v15;

        var v17: ISuper;
        var v18:ISuper = v17;
        v18.movedMethod();

        var v19: SourceClass;
        var v20:SourceClass = v19;
        v20.notMovedMethod();

        var v21: ISuper;
        var v22: IMoved = v21;

        var v23: SourceClass;
        var v24: INotMoved = v23;
    }

    public function test2() {
        var v7:ISuper;
        var v8:*;
        v8 = v7;

        var v7_:ISuper;
        var v8_:Object;
        v8_ = v7_;

        var v9:ISuper;
        var v10:ISuper;
        v10 = v9;

        var v11:SourceClass;
        var v12:int;
        v12 = v11;

        var v13:SourceClass;
        var v14:UnexistingType;
        v14 = v13;

        var v15:SourceClass;
        var v16:VariablesUsages2;
        v16 = v15;

        var v17:ISuper;
        var v18:ISuper;
        v18 = v17;
        v18.movedMethod();

        var v19:SourceClass;
        var v20:SourceClass;
        v20 = v19;
        v20.notMovedMethod();

        var v21:ISuper;
        var v22:IMoved;
        v22 = v21;

        var v23:SourceClass;
        var v24:INotMoved;
        v24 = v23;
    }
}
}

private class SourceClassEx extends SourceClass {}