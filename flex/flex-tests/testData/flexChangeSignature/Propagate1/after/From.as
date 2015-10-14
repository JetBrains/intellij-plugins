package {
class From {

    function foo(s:String, added1:Number, added2:Object) {
    }

    /**
     *
     * @param p
     *
     * @see #abc
     */
    function bar(p:int, added1:Number, added2:Object) { // propagate
        foo("", added1, added2);
        zzz(12, false, added1, added2);
    }

    function zzz(p:int, p2:Boolean, added1:Number, added2:Object) { // propagate
        foo("qq", added1, added2);
        bar(0, added1, added2);
        zzz(p, p2, added1, added2);
        abc(added1, added2);
    }

    function abc(added1:Number, added2:Object) { // propagate
        foo("foo", added1, added2);
        bar(100, added1, added2);
        zzz(200, true, added1, added2);
        abc(added1, added2);
    }

    function nopropagate() {
        foo("rr", added1def, added2def);
        bar(-5, added1def, added2def);
        zzz(-6, null, added1def, added2def);
        abc(added1def, added2def);
    }
}

}
}