// "Convert to local" "true"
class A {

    public function foo():void {
        var a = null;
        trace(a);
    }

    public function bar():void {
        <caret>var a = "";
        trace(a);
    }
}