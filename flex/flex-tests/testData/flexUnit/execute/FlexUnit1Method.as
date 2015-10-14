package {
import flexunit.framework.TestCase;

public class FlexUnit1Method extends TestCase {

    public function testGreen() {
        assertEquals(1, 1);
    }

    public function testRed() {
        assertEquals(1, 2);
    }

    public function testException() {
        throw new Error("error");
    }
}
}

<testResults status="Assertion failed">
  <suite name="FlexUnit1Method" status="Assertion failed">
    <test name="testRed" status="Assertion failed"/>
  </suite>
</testResults>
