package {
public class VariablesUsages {
    public function test() {
        var v1 : ISuper;
        v1.movedMethod();

        var v2: SourceClass;
        v2.movedMethod();
        v2.notMovedMethod();

        var v3: ISuper;
        v3.methodFromIMoved();

        var v4: SourceClass;
        v4.methodFromINotMoved();

        var v5: ISuper;
        v5.methodFromIMovedAndINotMoved();

        var v6: SourceClass;
        v6.movedMethod();
        v6.unexistingMethod();

        var v7: SourceClass;
        v7.unexistingProp;

        var v8: ISuper;
        v8.movedProp = 0;
        trace(v8.movedProp);

        var v9: SourceClass;
        v9.notMovedProp = 0;

    }
}
}