package {
[RunWith("<error>MyRunner</error>")]
class <warning descr="Test class should be public">MethodsWithCustomRunner</warning> {

    [Test]
    function testNonPublic() {}

    [Test]
    public static function testStatic():void {}

    [Test]
    public function testWithParam(p:String) {}

    [Test]
    public function testReturnType() : Number { return 0; }

    [Test]
    public function get testGet() : Number { return 1; }

    [Test]
    public function set testSet(p :Number) {}

    static function testFoo() {}

    [Test]
    [Ignore]
    static function zzz(p : Number) : Number {return 0;}

}
}