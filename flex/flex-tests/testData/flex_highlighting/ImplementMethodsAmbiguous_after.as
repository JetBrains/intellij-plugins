package {
import com.bar.A;
import com.foo.A;
import com.uu.A;
import com.zz.A;

public class ImplementMethodsAmbiguous implements Interface1{


    public function foo(p1:com.bar.A, p2:com.zz.A):com.foo.A {
        return undefined;
    }

    public function bar():com.uu.A {
        return undefined;
    }
}
}