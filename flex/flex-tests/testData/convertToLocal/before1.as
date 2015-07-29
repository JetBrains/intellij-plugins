// "Convert to local" "true"
class A {
    private var a<caret>:int = 4;

    public function foo():void {
        a = 7;
        f(a);
    }
}