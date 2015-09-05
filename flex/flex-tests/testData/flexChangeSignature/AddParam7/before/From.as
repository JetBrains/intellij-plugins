package
{
public class From
{
    function d<caret>oSmth(i: int, s: String, i2:int = 0, p:From=null) {}

    function zz() {
        doSmth();
        doSmth(0);
        doSmth(0, 1);
        doSmth(0, "abc");
        doSmth(0, true);
        doSmth(0, 1, 2);
        doSmth(0, 1, 2, 3);
        doSmth(0, 1, 2, this);
        doSmth(0, 1, 2, this, this);
    }
}
}
