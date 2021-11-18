package {

import flexunit.framework.TestSuite;

public class MethodInSuite2 extends TestSuite {

    [Test]
    public function <warning descr="Test method in suite">testFoo</warning>() {
    }

    public function <warning descr="Method can be made 'static'">testBar</warning>() { /*not a test method actually*/
      new MethodInSuite2();
    }
}
}