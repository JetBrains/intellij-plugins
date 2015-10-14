package {

public class NoChangeSignatureFixForVoidType {
    public function NoChangeSignatureFixForVoidType() {
        bar(<error>foo()</error>);
    }

    function foo():void {}
    function bar(a:int):void {}
}
}
