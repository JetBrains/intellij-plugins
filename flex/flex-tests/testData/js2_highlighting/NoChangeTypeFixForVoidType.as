package {

public class NoChangeTypeFixForVoidType {
    public function A() {
        var v : int  = <error>bar()</error>;
    }
    function bar():void {
    }
}
}
