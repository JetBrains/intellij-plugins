package {
import bar.IFoo;

import com.Foo;
import com.MyInt;
import com.MySuperInt3;

[RunWith("com.Foo")]
public class Usage {
    public function Usage() {
        var p: IFoo;
        p.moved1(null);

        var r: IFoo = null;
        f.moved2();

        var p2: Foo;
        p2.unexsistingMethod();

        var b: Foo;
        b.notMoved();

        var n = new Foo();
        new Foo();
        new Foo();

        var v;
        if (v instanceof Foo) {}
        if (v instanceof Foo) {}

        var v1 : IFoo;
        var z1 = v1;

        var v2 : Foo;
        var z2: int = v2;

        var v3: IFoo;
        var v4: IFoo = v3;

        var v5 : Foo;
        var v6: UnexistingType = v5;

        var v7: IFoo;
        var v8: MyInt = v7;

        var v9: Foo;
        var v10: MyInt2 = v9;

        var v11: Foo;
        var v12: Usage = v11;

        var v13: IFoo;
        var v14: MySuperInt3 = v13;

        Foo.staticFunc();
        trace(Foo.staticProp);
        trace(Foo.ID);
    }

    private function trace(p:*) {
        return Foo.staticProp;
    }
}
}