package {

import com.foo.A;
import com.bar.A;

public class AddTypeToDeclarationAmbiguous {


    public function foo(): com.bar.A {
        var v : com.foo.A;
        return null;
    }

    public function test(): void {
        var <warning>v</warning><caret> = foo();
    }


}
}