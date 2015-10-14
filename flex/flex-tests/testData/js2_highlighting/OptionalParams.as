package {
class OptionalParams{
    public function foo():void {
        bar<error descr="Invalid number of arguments, expected 3..4">()</error>;
        bar<error descr="Invalid number of arguments, expected 3..4">(1)</error>;
        bar<error descr="Invalid number of arguments, expected 3..4">(1, true)</error>;
        bar(1, 2, 3);
        bar(1, 2, 3, 4);
        baz<error descr="Invalid number of arguments, expected 1 or more">()</error>;
        baz(1);
        baz(1, 2);
        baz(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    public function bar(a:Number, b:Boolean, c:Number, d:int = 5): void {
        if (b){}
        if (c){}
    }

    public function baz(i:int, ...rest):void {
    }

}
}