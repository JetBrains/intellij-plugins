package {

class <warning descr="Test class should be public">Methods3</warning> {

    [Test]
    function <warning descr="Test method should be public">testNonPublic</warning>() {}

    [Test]
    public static function <warning descr="Test method should not be static">testStatic</warning>():void {}

    [Test]
    public function <warning descr="Test method should not have required parameters">testWithParam</warning>(p:String) {}

    [Test]
    public function <warning descr="Test method should return void">testReturnType</warning>() : Number { return 0; }

    [Test]
    public function get <warning descr="Test method should not be getter or setter">testGet</warning>() : Number { return 1; }

    [Test]
    public function set <warning descr="Test method should not have required parameters"><warning descr="Test method should not be getter or setter">testSet</warning></warning>(p :Number) {}

    static function testFoo() {}

    [Test]
    [Ignore]
    static function zzz(p : Number) : Number {return 0;}

}
}