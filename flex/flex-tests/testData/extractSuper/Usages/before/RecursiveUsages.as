package {
public class RecursiveUsages {
    public function RecursiveUsages() {
        var v2: SourceClass;
        v2 = v3;
        v3 = v2;

        var v4: SourceClass;
        var v5: SourceClass;
        v5 = v4;
        v4 = v5;

        var v6: SourceClass;
        var v7: SourceClass;
        v6 = v7;
        v7 = v6;
        v7.notMovedMethod();
    }
}
}