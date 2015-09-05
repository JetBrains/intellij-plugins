package {
public namespace MyNs = "aaaaaaa";
}

package {
public class From {
    MyNs var v;
    public function f<caret>oo(p) {
    }

    public function bar():void {
        foo(MyNs::v);
    }
}
}

