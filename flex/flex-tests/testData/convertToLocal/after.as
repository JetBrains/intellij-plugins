// "Convert to local" "true"
class A {

    public function foo():void {
        <caret>var a:* = 4;
        var b = a * 7;
    }
}