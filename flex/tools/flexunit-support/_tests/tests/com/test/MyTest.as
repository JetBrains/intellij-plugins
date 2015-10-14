/**
 * Created with IntelliJ IDEA.
 * User: Kirill.Safonov
 * Date: 6/7/12
 * Time: 8:43 PM
 * To change this template use File | Settings | File Templates.
 */
package com.test {
import flexunit.framework.TestCase;


//import mx.logging.Log;

public class MyTest {

    public function testFoo():void {
        var v = 0;
    }

    [Test]
    public function testBar():void {
        var v =56;
        for (var i = 0; i < 1000000; i++) {
            v = Math.sin(v);
        }
    }
    [Test]
    public function testBar2():void {
        var v = 4;
        for (var i = 0; i < 1000000; i++) {
            v = Math.sin(v);
        }
    }
    [Test]
    public function testBar3():void {
        var v = 3;
        for (var i = 0; i < 1000000; i++) {
            v = Math.sin(v);
        }
    }
    [Test]
    public function testBar4():void {
        var v= 45;
        for (var i = 0; i < 100000; i++) {
            v = Math.sin(v);
        }
    }

    public function giveMeFoo():String {
        return "foo";
    }

    public function computeBar():String {
        return "bar";
    }

    public function failMePlease():void {
        throw new Error("Such an epic fail");
    }

}
}
