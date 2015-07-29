// "Convert to local" "true"
class A {
    private var a<caret>:int = 0;

    public function foo():void {
        trace(a);
    }

    public function bar():void {
        trace(a);
    }
}