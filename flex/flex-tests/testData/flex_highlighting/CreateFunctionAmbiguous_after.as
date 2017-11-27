package {

import com.foo.A;
import com.bar.A;
import com.zz.A;

public class CreateFunctionAmbiguous {

    public function test(): void {
        var p : com.bar.A;
        var p2 : com.zz.A;
        var v: com.foo.A = foo(p, p2);
    }

    private function foo(p:com.bar.A, p2:com.zz.A):com.foo.A {
        return undefined;
    }

}
}