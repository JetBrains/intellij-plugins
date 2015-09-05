package {
interface IFoo {

    function foo(p:int, s:String, ...r):Number;
}

public class Sub implements IFoo {
    /**
     *
     * @param p p
     * @param s s
     * @param r r
     * @return return
     */
    public final function foo(p:int, s:String, ...r):Number {
        var v = 0;
        return 0;
    }
}

}
