// "Convert to local" "true"
class A {

    public function foo():void {
        <caret>var a:int = 7;
        f(a);
    }
}