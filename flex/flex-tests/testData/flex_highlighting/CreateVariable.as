package {

import test.<symbolName descr="classes//class name">RestParam</symbolName>;

public class <symbolName descr="classes//class name">CreateVariable</symbolName> {

    public function <symbolName descr="instance method">foo</symbolName>():void{
        <symbolName descr="classes//class name">RestParam</symbolName>.<symbolName descr="static method">foo</symbolName>(<error descr="Unresolved variable or type myfield">my<caret>field</error>);
    }

}
}
