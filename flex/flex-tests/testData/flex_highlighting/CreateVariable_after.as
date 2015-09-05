package {

import test.RestParam;

public class CreateVariable {
    private var myfield<caret>:;

    public function foo():void{
        RestParam.foo(myfield);
    }

}
}
