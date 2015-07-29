package {

import org.flexunit.Assert;

[RunWith("CustomRunner")]
public class ClassWithCustomRunner {

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
  <suite name="ClassWithCustomRunner" status="Assertion failed">
    <test name="foo1" status="Completed"/>
    <test name="foo2" status="Assertion failed"/>
  </suite>
</testResults>