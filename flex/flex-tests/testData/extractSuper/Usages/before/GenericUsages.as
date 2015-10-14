package {
public class GenericUsages {
    public function GenericUsages() {
        var v1:Vector.<SourceClass> = new Vector.<SourceClass>();
        foo(v1[0]);
        v1.clear();
        v1.pop().foobar();

        var v2:Vector.<SourceClass> = new Vector.<SourceClass>();
        v2[0].movedMethod();

        var v3:Vector.<SourceClass> = new Vector.<SourceClass>();
        v3[50].notMovedProp = 0;

        var v4:Vector.<SourceClass> = new Vector.<SourceClass>();
        var p : SourceClass = v4[50];
    }

    function foo(p:*) {}
}
}