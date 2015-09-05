package a {
public class From {

    /*before*/
    private static var field = method1();
    /*after*/

    var before1;public static const c;/*after*/
    var before2; public static const c2; /*after*/

    var before3;private static function method1() {
        field = 0;
        method2();
    }/*after*/

    var before4; private static function method2() {
        method1();
    } /*after*/

    var before5;private static function method3() {
        method3();
    } /*after*/
}
}