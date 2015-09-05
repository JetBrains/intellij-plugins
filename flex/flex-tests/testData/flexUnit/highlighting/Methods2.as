package {

import net.digitalprimates.fluint.tests.TestCase;

class <warning descr="Test class should be public">Methods2</warning> extends TestCase {

    
    function <warning descr="Test method should be public">testNonPublic</warning>() {}

    public static function <warning descr="Test method should not be static">testStatic</warning>():void {}

    public function <warning descr="Test method should not have required parameters">testWithParam</warning>(p:String) {}

    public function testReturnType() : Number { return 0; }

    public function get <warning descr="Test method should not be getter or setter">testGet</warning>() : Number { return 1; }

    public function set <warning descr="Test method should not have required parameters"><warning descr="Test method should not be getter or setter">testSet</warning></warning>(p :Number) {}

    [Test]
    public function testWithAnnotation() {}

}
}