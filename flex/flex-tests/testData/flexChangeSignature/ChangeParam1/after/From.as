package {
import flash.events.Event;
import flash.events.EventDispatcher;

class From extends FromBase {

    /**
     * @param sss s_param
     * @param i2 i param
     * @param o o param
     * @param rest2 rest params
     */
    override function renamed(i2:Number, sss:String = "abc", o:EventDispatcher = FOO, ...rest2):Boolean {
    // s
        Alert.show(sss);
        trace(i2);
        if (o != null) {

        }
        trace(rest2);
    }

    function bar(p:*) {
        renamed(0, "abc", new Event(), this, null);

        var p1, p2, p3, p4, p5;
        renamed(123, "", new Event(), p1, p2, p3, p4, p5);
    }
}
}
