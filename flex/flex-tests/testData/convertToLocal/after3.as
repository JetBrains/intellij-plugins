// "Convert to local" "true"
class A {

    public function foo():void {
        var a:int = 0;
        trace(a);
    }

    public function bar():void {
        <caret>var a:int = 0;
        trace(a);
    }
}