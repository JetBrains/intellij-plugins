package {
public namespace MyNs = "aaaaaaa";
}

package {
public class From {
    MyNs var f;
    MyNs function foo(p) {
    }

    public function bar():void {
        MyNs::fo<caret>o(MyNs::f);
    }
}
}

package {
public class Zzz {
    public function barZ():void {
        var v : From;
        v.MyNs::foo(null);
    }
}
}