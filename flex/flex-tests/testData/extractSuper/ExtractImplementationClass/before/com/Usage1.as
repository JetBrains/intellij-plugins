package com {
public class Usage1 {
    public function ooo(p:From):From {
        ooo(p);

        p.notMovedProp = 0;

        var v1: From;
        v1.movedMethod();

        var v2: From;
        v2.notMovedMethod();

        var v3 = new From();
        if (v3 instanceof From) {}

        var v4 : From;
        v4 = v4;
        v4.movedProp = 0;

        return new From();
    }

}
}