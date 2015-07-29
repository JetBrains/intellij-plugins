package {
class From {

    function f<caret>oo(s: String) {
    }

    /**
     *
     * @param p
     *
     * @see #abc
     */
    function bar(p: int) { // propagate
        foo("");
        zzz(12, false);
    }

    function zzz(p: int, p2: Boolean) { // propagate
        foo("qq");
        bar(0);
        zzz(p, p2);
        abc();
    }

    function abc() { // propagate
        foo("foo");
        bar(100);
        zzz(200, true);
        abc();
    }

    function nopropagate() {
        foo("rr");
        bar(-5);
        zzz(-6, null);
        abc();
    }
}

}
}