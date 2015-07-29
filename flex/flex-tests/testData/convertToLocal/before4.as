// "Convert to local" "true"
class A {
    private var a<caret>;

    public function foo():void {
        a = null;
        trace(a);
    }

    public function bar():void {
        a = "";
        trace(a);
    }
}