package {

public class TestPanelBase {
    public var testDa<caret>ta : Object;
}

public class UnrelatedTestClass {
    public var testData : Object;
}

public class UnrelatedTestSubclass extends UnrelatedTestClass {
    public function setTestData(data : TestData) : void {
        testData = data;
    }
}

}