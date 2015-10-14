package {
public class UnicodeBreaks {

  import flexunit.framework.Assert;

  [Test]
  public function test1() {
    Assert.assertEquals("X", String.fromCharCode(0x2028));

  }

  [Test]
  public function test2() {
    Assert.assertEquals("X", String.fromCharCode(0x2029));

  }

}
}

<testResults status="Assertion failed">
  <suite name="UnicodeBreaks" status="Assertion failed">
    <test name="test1" status="Assertion failed"/>
    <test name="test2" status="Assertion failed"/>
  </suite>
</testResults>
