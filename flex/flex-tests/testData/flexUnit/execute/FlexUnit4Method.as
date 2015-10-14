package {
import org.flexunit.Assert;
public class FlexUnit4Method {

    [Test]
    public function green() {
        Assert.assertEquals(1, 1);
    }

    [Test]
    public function red() {
        Assert.assertEquals(1, 2);
    }

    [Test]
    public function exception() {
        throw new Error("error");
    }

    public function notATest() {

    }
}
}

<testResults status="Assertion failed">
  <suite name="FlexUnit4Method" status="Assertion failed">
      <test name="red" status="Assertion failed"/>
  </suite>
</testResults>
