package {
import org.flexunit.Assert;
public class FlexUnit4TestIgnored3 {

    [Test]
    [Ignore]
    public function method1() {

    }

    [Test]
    public function method2() {
        Assert.assertEquals(1, 2);
    }

    [Test]
    [Ignore]
    public function method3() {

    }

    [Test]
    public function method4() {

    }


}
}

<testResults status="Assertion failed">
  <suite name="FlexUnit4TestIgnored3" status="Assertion failed">
      <test name="method1" status="Ignored"/>
      <test name="method2" status="Assertion failed"/>
      <test name="method4" status="Completed"/>
      <test name="method3" status="Ignored"/>
  </suite>
</testResults>