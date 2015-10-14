package {

import net.digitalprimates.fluint.tests.TestSuite;

public class MethodInSuite3 extends TestSuite {

    [Test]
    public function <warning descr="Test method in suite">testFoo</warning>() {
    }

    public function testBar() { /*not a test method actually*/
    }
}
}