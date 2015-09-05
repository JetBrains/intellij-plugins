package {
public class Usages {
    public function Usages(p:IFrom):IFrom {
        var v1 : IFrom;
        v1.movedMethod();

        if (v1 instanceof IFrom) {}

        var v2 : IFrom = v1;
        v2 = null;

        var v3 : IFrom;
        v3.notMovedMethod();

        p.movedProp();

        return 0;
    }
}
}