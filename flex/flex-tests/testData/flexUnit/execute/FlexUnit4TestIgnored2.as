package {
import org.flexunit.Assert;
public class FlexUnit4TestIgnored2 {

    [Test]
    public function method1() {
        Assert.assertEquals(1, 2);
    }

    [Test]
    [Ignore]
    public function method2() {

    }

}
}

<testResults status="Assertion failed">
  <suite name="FlexUnit4TestIgnored2" status="Assertion failed">
    <test name="method1" status="Assertion failed"/>
    <test name="method2" status="Ignored"/>
  </suite>
</testResults>