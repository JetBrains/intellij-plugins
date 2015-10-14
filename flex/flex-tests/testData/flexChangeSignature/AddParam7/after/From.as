package
{
public class From
{
    function doSmth(s:String, i:int, b:Type, i2:int = 0, p:From = null) {}

    function zz() {
        doSmth();
        doSmth(0);
        doSmth(1, 0, def);
        doSmth("abc", 0, def);
        doSmth(true, 0, def);
        doSmth(1, 0, def, 2);
        doSmth(1, 0, def, 2, 3);
        doSmth(1, 0, def, 2, this);
        doSmth(0, 1, 2, this, this);
    }
}
}
