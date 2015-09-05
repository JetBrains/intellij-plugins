package {

import flexunit.framework.TestSuite;
import rawTests.TestFoo;

public class Suite2 extends TestSuite {

    public function Suite2() {
        addTestSuite(TestFoo);
    }
}

}