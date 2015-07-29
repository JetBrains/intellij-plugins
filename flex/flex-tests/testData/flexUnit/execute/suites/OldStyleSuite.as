package {

import flexunit.framework.TestSuite;
import rawTests.TestFoo;

public class OldStyleSuite extends TestSuite {

    public function OldStyleSuite() {
        addTestSuite(TestFoo);
    }
}

}

<testResults status="Completed">
    <suite name="rawTests.TestFoo" status="Completed">
        <test name="testOne" status="Completed"/>
    </suite>
</testResults>