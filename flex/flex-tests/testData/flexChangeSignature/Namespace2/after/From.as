package {
public namespace MyNs = "aaaaaaa";
}

package {
public class From {
    MyNs var v;
    private function foo2(p) {
    }

    public function bar():void {
        foo2(MyNs::v);
    }
}
}

