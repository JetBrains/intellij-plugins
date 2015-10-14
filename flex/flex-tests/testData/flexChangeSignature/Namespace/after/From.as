package {
public namespace MyNs = "aaaaaaa";
}

package {
public class From {
    MyNs var f;
    public function foo2(p) {
    }

    public function bar():void {
        foo2(MyNs::f);
    }
}
}

package {
public class Zzz {
    public function barZ():void {
        var v : From;
        v.foo2(null);
    }
}
}