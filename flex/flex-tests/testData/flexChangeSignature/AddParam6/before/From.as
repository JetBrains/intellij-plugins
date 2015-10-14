package
{
public class A
{
    public function A(i:int, b:Boolean) // propagate
    {
        doSmth();
        doSmth(1);
        doSmth(1, 2);
        doS<caret>mth(1, 2, 3);
        doSmth(1, 2, 3, 4);
    }

    public function B()
    {
        doSmth();
        doSmth(1);
        doSmth(1, 2);
        doSmth(1, 2, 3);
        doSmth(1, 2, 3, 4);
    }

    function doSmth(...args) {}

    function zz() {
        new A();
        new A(1);
        new A(true);
        new A(1, true);
        new A(1, true, "zzz");
    }
}
}
