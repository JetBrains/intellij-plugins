package {
public class From {

    public function foo(p:From, p2:From) {
        var v1:From;
        v1.foo();
        var v2:From;
        v2.bar();
        var v3:From;
        v3.zzz();

        var v4: From = new From();

        var s : From = p;
        s.foo();
        var s2 : From = p2;
        s2.zzz();
    }

    function bar():void {
    }

    function zzz():void {
    }

}
}
