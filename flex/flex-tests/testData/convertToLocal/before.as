// "Convert to local" "true"
class A {
    private var a<caret>:* = 4;

    public function foo():void {
        var b = a * 7;
    }
}