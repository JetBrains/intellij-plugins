package {
public class FromSuper {
    public function FromSuper() {
    }

    public function foo(p:FromSuper, p2:From) {
        var v1:FromSuper;
        v1.foo();
        var v2:FromSuper;
        v2.bar();
        var v3:From;
        v3.zzz();

        var v4:FromSuper = new From();

        var s:FromSuper = p;
        s.foo();
        var s2:From = p2;
        s2.zzz();
    }

    function bar():void {
    }
}
}
