package {
public class CastUsages {
    public function CastUsages() {
        var ref:ISuper;

        var t : * = ISuper(ref);
        t = ISuper(ref);

        var t2 = ISuper(ref);
        t2 = ISuper(ref);

        var t3:int = SourceClass(ref);
        t3 = SourceClass(ref);

        var t4:UnexistingType = SourceClass(ref);
        t4 = SourceClass(ref);

        var t5:IMoved = ISuper(ref);
        t5 = ISuper(ref);

        var t6:INotMoved = SourceClass(ref);
        t6 = SourceClass(ref);

        var t7:CastUsages = SourceClass(ref);
        t7 = SourceClass(ref);

    }
}
}