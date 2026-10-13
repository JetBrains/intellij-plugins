package p {

<weak_warning>class A implements I1, I2, I3 {
    function g():Boolean {
        var a = "hello";
        var b: String = "hello";
        var c: String = "123";
    }
}</weak_warning>

<weak_warning>class B implements I1, I3, I2{
    function g():Boolean {
        var a = "hello";
        var b: String = "hello";
        var c: String = "123";
    }
}</weak_warning>

}