package {
public class VariablesUsages2 {
    public function test() {
        var v1: SourceClass;
        v1 = unexistingVariable;

        var v2 : SourceClass;
        v2 = null;

        var v3: SourceClass;
        v3 = 0;

        var v4: SourceClass;
        v4 = new SourceClass();

        var v5: SourceClass;
        v5 = new SourceClassEx();

        var v6: SourceClass;
        v6 = new VariablesUsages2();

        var v7: SourceClass;
        var v8: * = v7;

        var v7_: SourceClass;
        var v8_: Object = v7_;

        var v9: SourceClass;
        var v10 = v9;

        var v11: SourceClass;
        var v12:int = v11;

        var v13: SourceClass;
        var v14:UnexistingType = v13;

        var v15: SourceClass;
        var v16:VariablesUsages2 = v15;

        var v17: SourceClass;
        var v18:SourceClass = v17;
        v18.movedMethod();

        var v19: SourceClass;
        var v20:SourceClass = v19;
        v20.notMovedMethod();

        var v21: SourceClass;
        var v22: IMoved = v21;

        var v23: SourceClass;
        var v24: INotMoved = v23;
    }

    public function test2() {
        var v7:SourceClass;
        var v8:*;
        v8 = v7;

        var v7_:SourceClass;
        var v8_:Object;
        v8_ = v7_;

        var v9:SourceClass;
        var v10:SourceClass;
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

        var v17:SourceClass;
        var v18:SourceClass;
        v18 = v17;
        v18.movedMethod();

        var v19:SourceClass;
        var v20:SourceClass;
        v20 = v19;
        v20.notMovedMethod();

        var v21:SourceClass;
        var v22:IMoved;
        v22 = v21;

        var v23:SourceClass;
        var v24:INotMoved;
        v24 = v23;
    }
}
}

private class SourceClassEx extends SourceClass {}