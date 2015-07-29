package {

[Suite]
[RunWith("org.flexunit.runners.Suite")]
public class MethodInSuite {

    public var test : MyTest;

    [Test]
    public function <warning descr="Test method in suite">testFoo</warning>() {
    }
}
}