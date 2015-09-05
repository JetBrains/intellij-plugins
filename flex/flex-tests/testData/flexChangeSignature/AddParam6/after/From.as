package
{
public class A
{
    public function A(i:int, b:Boolean, s:String) // propagate
    {
        doSmth(s);
        doSmth(s, 1);
        doSmth(s, 1, 2);
        doSmth(s, 1, 2, 3);
        doSmth(s, 1, 2, 3, 4);
    }

    public function B()
    {
        doSmth("abc");
        doSmth("abc", 1);
        doSmth("abc", 1, 2);
        doSmth("abc", 1, 2, 3);
        doSmth("abc", 1, 2, 3, 4);
    }

    function doSmth(s:String, ...args) {}

    function zz() {
        new A();
        new A(1);
        new A(true);
        new A(1, true, "abc");
        new A(1, true, "zzz");
    }
}
}
