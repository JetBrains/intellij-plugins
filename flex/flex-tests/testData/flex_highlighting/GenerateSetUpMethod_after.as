package {
import flexunit.framework.TestCase;

public class GenerateSetUpMethod extends TestCase{

    [Before]
    public override function setUp():void {
        super.setUp();<caret>
    }

    [After]
    public override function tearDown():void {
        super.tearDown();
    }
}
}