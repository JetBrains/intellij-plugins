package {

import test.<info descr="classes//class name">RestParam</info>;

public class <info descr="classes//class name">CreateVariable</info> {

    public function <info descr="instance method">foo</info>():void{
        <info descr="classes//class name">RestParam</info>.<info descr="static method">foo</info>(<error descr="Unresolved variable or type myfield">my<caret>field</error>);
    }

}
}
