package {
public class UsageBase implements IUsage {
    var _v : FooBase;
    public function zz(p1:FooBase, p2:Foo, p3:Foo, p4:Foo) {
        p1.foo();
        p2.bar();
        p3.foo();
        var _v = p4;
        p1 = p1;
        p4 = p4;
    }
}
}
