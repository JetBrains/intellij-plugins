package {
import flash.events.Event;

class From extends FromBase {

    /**
     * @param s s_param
     * @param i i param
     * @param o o param
     * @param rest rest params
     */
    override function foo(s:String, i:Nu<caret>mber, o:Event, ...rest):void {
    // s
        Alert.show(s);
        trace(i);
        if (o != null) {

        }
        trace(rest);
    }

    function bar(p:*) {
        foo("abc", 0, new Event(), this, null);

        var p1, p2, p3, p4, p5;
        foo("", 123, new Event(), p1, p2, p3, p4, p5);
    }
}
}
