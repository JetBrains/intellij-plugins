package bar {
import com.MyInt;
import com.SomeClass;

public interface IFoo extends MyInt {
    /**
     * blabla
     * @param p
     */
    function moved1(p:SomeClass);

    /**
     * zzzz
     * @param v
     * @param p
     */
    function moved2(v:String = "abc", ...p);

    function moved3();

    function moved4();

    function moved5();
}
}
