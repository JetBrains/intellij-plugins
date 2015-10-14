package {
public interface IBase {
    /**
     *
     * @param i
     * @return
     */
    function foo(i:int):String;
}

public interface ISub extends IBase {
}
}
