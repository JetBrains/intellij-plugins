package {
import org.flexunit.Assert;
public class FlexUnit4TestIgnored1 {

    [Test]
    public function method2() {
        Assert.assertEquals(1, 2);
    }

    [Test]
    [Ignore]
    public function method1() {

    }

}
}

<testResults status="Assertion failed">
  <suite name="FlexUnit4TestIgnored1" status="Assertion failed">
      <test name="method1" status="Ignored"/>
      <test name="method2" status="Assertion failed"/>
  </suite>
</testResults>