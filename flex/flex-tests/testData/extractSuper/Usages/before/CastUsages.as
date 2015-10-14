package {
public class CastUsages {
    public function CastUsages() {
        var ref:SourceClass;

        var t : * = SourceClass(ref);
        t = SourceClass(ref);

        var t2 = SourceClass(ref);
        t2 = SourceClass(ref);

        var t3:int = SourceClass(ref);
        t3 = SourceClass(ref);

        var t4:UnexistingType = SourceClass(ref);
        t4 = SourceClass(ref);

        var t5:IMoved = SourceClass(ref);
        t5 = SourceClass(ref);

        var t6:INotMoved = SourceClass(ref);
        t6 = SourceClass(ref);

        var t7:CastUsages = SourceClass(ref);
        t7 = SourceClass(ref);

    }
}
}