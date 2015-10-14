package {

import net.digitalprimates.fluint.tests.TestSuite;
import rawTests.FlunitTest;

public class FlunitSuite extends TestSuite {
    public function FlunitSuite() {
        addTestCase(new FlunitTest());
    }
}
}

<testResults status="Completed">
    <suite name="rawTests.FlunitTest" status="Completed">
        <test name="foo" status="Completed"/>
    </suite>
</testResults>