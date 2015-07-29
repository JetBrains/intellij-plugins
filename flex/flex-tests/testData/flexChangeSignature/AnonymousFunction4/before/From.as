package {

public class From {

    function foo(p2:String) { // propagate
        var v:Function = function(a:int, b:String<caret>):void {
                trace(a+b);
        };
        v(1, 2);

        if (true) {
            v("a", "b");
        };
    }

    function bar() {
      foo("!");
    }

}
}
