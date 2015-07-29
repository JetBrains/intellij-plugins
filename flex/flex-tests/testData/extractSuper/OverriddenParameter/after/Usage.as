package {
public class Usage extends UsageBase {
    var _v : FooBase;
    public override function zz(p1:FooBase, p2:Foo, p3:Foo, p4:Foo) {
        p1.foo();
        p2.foo();
        p3.bar();
        var _v = p4;
    }
}
}
