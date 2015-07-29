package {

import org.flexunit.Assert;

[RunWith("CustomRunner")]
public class MethodWithCustomRunner {

    [Foo]
    public function foo1() {
    }

    [Foo]
    public function foo2() {
        Assert.assertTrue(false);
    }

    [Test]
    public function foo3() {
    }


}
}

<testResults status="Assertion failed">
  <suite name="MethodWithCustomRunner" status="Assertion failed">
    <test name="foo2" status="Assertion failed"/>
  </suite>
</testResults>