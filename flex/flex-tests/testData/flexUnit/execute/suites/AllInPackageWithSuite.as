package {

import rawTests.TestFoo;
import rawTests.TestBar;

[Suite]
[RunWith("org.flexunit.runners.Suite")]
public class AllInPackageWithSuite {

    public var test1 : TestFoo;
    public var test2 : TestBar;
}
}

<testResults status="Assertion failed">
    <suite name="rawTests.TestFoo" status="Completed">
        <test name="testOne" status="Completed"/>
    </suite>
    <suite name="rawTests.TestBar" status="Assertion failed">
        <test name="bar" status="Assertion failed"/>
    </suite>
    <suite name="rawTests.FlunitTest" status="Completed">
        <test name="foo" status="Completed"/>
    </suite>
</testResults>