package com {
import bar.FromImpl;

public class Usage1 {
    public function ooo(p:FromImpl):From {
        ooo(p);

        p.notMovedProp = 0;

        var v1: From;
        v1.movedMethod();

        var v2: FromImpl;
        v2.notMovedMethod();

        var v3 = new FromImpl();
        if (v3 instanceof FromImpl) {}

        var v4 : From;
        v4 = v4;
        v4.movedProp = 0;

        return new FromImpl();
    }

}
}