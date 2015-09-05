package {

public class From {

    function foo(p2:String, c:Boolean) { // propagate
        var v2:Function = function (b2:String, a2:int, c:Boolean):String {
                trace(a2+b2);
        };
        v2(2, 1, c);

        if (true) {
            v2("b", "a", c);
        };
    }

    function bar() {
      foo("!", false);
    }

}
}
